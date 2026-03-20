package com.gd.self.tool.tx.service.imp;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gd.self.tool.common.Constant;
import com.gd.self.tool.tx.domain.CategoryInfo;
import com.gd.self.tool.tx.domain.RankingSoftware;
import com.gd.self.tool.tx.domain.RecommendedSoftware;
import com.gd.self.tool.tx.domain.SoftwareInfo;
import com.gd.self.tool.tx.domain.SoftwareTag;
import com.gd.self.tool.tx.mapper.SoftwareInfoMapper;
import com.gd.self.tool.tx.mapper.SoftwareTagMapper;
import com.gd.self.tool.tx.service.SoftwareDetailCrawlerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯软件中心详情页爬虫
 * 功能：提取软件详情页的各项信息
 * 包括：大小、操作系统、版本、更新时间、软件详情、轮播图等
 */
@Service
public class SoftwareDetailCrawlerImpl implements SoftwareDetailCrawlerService {
    @Autowired
    private SoftwareInfoMapper softwareInfoMapper;
    @Autowired
    private SoftwareTagMapper softwareTagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update() {
        Long count = softwareInfoMapper.countAll();
        Long size = 200L;
        long totalPage = NumberUtil.div(count, size, 1, RoundingMode.CEILING).longValue();
        LambdaQueryWrapper<SoftwareInfo> queryWrapper = new LambdaQueryWrapper<>();
        List<SoftwareTag> allTag = new ArrayList<>();
        for (int i = 0; i < totalPage; i++) {
            IPage<SoftwareInfo> page = new Page<SoftwareInfo>(i, size);
            IPage<SoftwareInfo> pageList = softwareInfoMapper.selectPage(page, queryWrapper);
            for (SoftwareInfo record : pageList.getRecords()) {
                allTag.addAll(parseFromUrl(record));
            }
        }
        softwareTagMapper.insert(allTag);
    }

    /**
     * 从URL解析HTML
     */
    public List<SoftwareTag> parseFromUrl(SoftwareInfo softwareInfo) {
        try {
            Document doc = Jsoup.connect(Constant.TX_BASE_URL + softwareInfo.getDetailUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            extractSoftwareInfo(doc, softwareInfo);
            softwareInfoMapper.updateById(softwareInfo);
            return softwareInfo.getTags();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 提取软件基本信息
     * 包括：大小、操作系统、版本、更新时间、软件详情
     */
    public SoftwareInfo extractSoftwareInfo(Document doc, SoftwareInfo info) {

        // 获取软件名称
        Element softNameElement = doc.select(".detail-name").first();
        if (softNameElement != null) {
            info.setSoftwareName(softNameElement.text());
        }

        // 获取软件图标
        Element logoImg = doc.select(".detail-logo img").first();
        if (logoImg != null) {
            info.setLogoUrl(logoImg.attr("src"));
        }

        // 获取大小
        Element sizeElement = doc.select(".soft-sys-info:contains(大小) .sys-info-value").first();
        if (sizeElement != null) {
            info.setSize(sizeElement.text());
        }

        // 获取操作系统
        Element osElement = doc.select(".soft-sys-info:contains(操作系统) .sys-info-value").first();
        if (osElement != null) {
            info.setOperatingSystem(osElement.text());
        }

        // 获取版本
        Element versionElement = doc.select(".soft-sys-info:contains(版本) .sys-info-value").first();
        if (versionElement != null) {
            info.setVersion(versionElement.text());
        }

        // 获取更新时间
        Element updateTimeElement = doc.select(".soft-sys-info:contains(更新时间) .sys-info-value").first();
        if (updateTimeElement != null) {
            info.setUpdateTime(DateUtil.parseLocalDateTime(updateTimeElement.text(), DatePattern.NORM_DATE_PATTERN));
        }

        // 获取软件详情描述
        Element descElement = doc.select(".desc-info").first();
        if (descElement != null) {
            info.setDescription(descElement.text());
        }

        List<SoftwareTag> tagInfos = new ArrayList<>();
        // 获取安全认证标签
        Elements tags = doc.select(".search-tags-item");
        for (Element tag : tags) {
            SoftwareTag tagInfo = new SoftwareTag();
            tagInfo.setTagName(tag.text());
            // 获取软件图标
            Element tagImg = tag.select("img").first();
            if (tagImg != null) {
                tagInfo.setTagUrl(tagImg.attr("src"));
            }
            tagInfos.add(tagInfo);
        }
        info.setTags(tagInfos);

        //获取轮播图
        List<String> sliderImages = extractSliderImages(doc);
        info.setSliderImageList(sliderImages);

        return info;
    }

    /**
     * 获取轮播图图片列表
     * 选择器：.slider-wrapper .slide img
     */
    public List<String> extractSliderImages(Document doc) {
        List<String> imageUrls = new ArrayList<>();

        Elements slides = doc.select(".slider-wrapper .slide img");
        for (Element slide : slides) {
            String imgSrc = slide.attr("src");
            if (imgSrc != null && !imgSrc.isEmpty()) {
                imageUrls.add(imgSrc);
            }
        }

        return imageUrls;
    }

    /**
     * 获取"你可能感兴趣"的推荐软件
     * 包括：软件名称、详情链接、图标地址、软件描述
     */
    public List<RecommendedSoftware> extractRecommendedSoftware(Document doc) {
        List<RecommendedSoftware> recommendedList = new ArrayList<>();

        Elements recommItems = doc.select(".detail-recomm-item");

        for (Element item : recommItems) {
            RecommendedSoftware rec = new RecommendedSoftware();

            // 获取软件ID
            String dataId = item.attr("data-id");
            rec.setSoftwareId(dataId);

            // 获取详情链接
            Element link = item.select("a.detail-recomm-info").first();
            if (link != null) {
                rec.setDetailUrl(link.attr("href"));
                rec.setSoftwareName(link.attr("title"));
            }

            // 获取图标
            Element logo = item.select(".detail-recomm-logo img").first();
            if (logo != null) {
                rec.setIconUrl(logo.attr("src"));
            }

            // 获取软件描述
            Element desc = item.select(".detail-recomm-time").first();
            if (desc != null) {
                rec.setDescription(desc.text());
            }

            // 获取下载标签
            Element downloadBtn = item.select(".J_qq_recom_download").first();
            if (downloadBtn != null) {
                rec.setDownloadTag(downloadBtn.attr("data-hottag"));
            }

            recommendedList.add(rec);
        }

        return recommendedList;
    }

    /**
     * 获取全站榜单软件
     */
    public List<RankingSoftware> extractRankingSoftware(Document doc) {
        List<RankingSoftware> rankingList = new ArrayList<>();

        // 热门榜单
        Elements hotItems = doc.select(".hot-top-soft-contain .soft-item");

        for (Element item : hotItems) {
            RankingSoftware rank = new RankingSoftware();
            rank.setRankType("热门");

            // 获取排名数字
            Element rankNo = item.select(".soft-no, .soft-no-1, .soft-no-2, .soft-no-3").first();
            if (rankNo != null) {
                rank.setRank(rankNo.text());
            }

            // 获取软件名称
            Element nameElement = item.select(".soft-name").first();
            if (nameElement != null) {
                rank.setSoftwareName(nameElement.text());
            }

            // 获取详情链接
            String detailUrl = item.attr("href");
            rank.setDetailUrl(detailUrl);

            // 获取软件ID
            Element downloadBtn = item.select(".J_qq_hot_download").first();
            if (downloadBtn != null) {
                rank.setSoftwareId(downloadBtn.attr("data-id"));
            }

            // 检查是否有热门标签
            if (item.select(".hot-tag").size() > 0) {
                rank.setHotTag(true);
            }

            rankingList.add(rank);
        }

        return rankingList;
    }

    /**
     * 获取软件分类列表
     */
    public List<CategoryInfo> extractCategoryList(Document doc) {
        List<CategoryInfo> categoryList = new ArrayList<>();

        Elements categoryItems = doc.select(".cat-list .cat-item");

        for (Element item : categoryItems) {
            CategoryInfo category = new CategoryInfo();

            Element link = item.select("a").first();
            if (link != null) {
                category.setCategoryName(link.text());
                category.setCategoryUrl(link.attr("href"));

                // 获取分类ID
                String catId = link.attr("data-catid");
                category.setCategoryId(catId);

                // 获取分类总数
                String total = link.attr("data-total");
                category.setTotalCount(total);
            }

            // 获取分类图标
            Element icon = item.select("img").first();
            if (icon != null) {
                category.setIconUrl(icon.attr("src"));
            }

            // 检查是否为当前分类
            if (item.hasClass("cat-curr")) {
                category.setCurrent(true);
            }

            categoryList.add(category);
        }

        return categoryList;
    }


}
