package com.gd.self.tool.tx.service.imp;

import com.gd.self.tool.common.Constant;
import com.gd.self.tool.tx.domain.SoftwareClass;
import com.gd.self.tool.tx.domain.SoftwareClassMapping;
import com.gd.self.tool.tx.domain.SoftwareInfo;
import com.gd.self.tool.tx.mapper.SoftwareClassMapper;
import com.gd.self.tool.tx.mapper.SoftwareClassMappingMapper;
import com.gd.self.tool.tx.mapper.SoftwareInfoMapper;
import com.gd.self.tool.tx.service.SoftwareCrawlerService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 腾讯软件中心爬虫脚本
 * 功能：
 * 1. 获取导航分类下的所有链接
 * 2. 获取软件列表中的软件信息（链接、标题、图片src）
 */
@Service
public class SoftwareCrawlerImp implements SoftwareCrawlerService {


    @Autowired
    private SoftwareClassMapper softwareClassMapper;

    @Autowired
    private SoftwareInfoMapper softwareInfoMapper;

    @Autowired
    private SoftwareClassMappingMapper softwareClassMappingMapper;

    /**
     * 从URL解析HTML
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void parseFromUrl(String url) {
        try {
            Document doc = buildDocument(Constant.TX_BASE_URL + url);

            // 获取导航分类链接
            List<SoftwareClass> navLinks = getNavCategoryLinks(doc);
            softwareClassMapper.insert(navLinks);
            List<SoftwareClassMapping> classMappings = new ArrayList<>();
            List<SoftwareInfo> allSoftwareList = new ArrayList<>();
            System.out.println("导航分类链接: " + navLinks);
            for (SoftwareClass navLink : navLinks) {
                Document softDoc = buildDocument(Constant.TX_BASE_URL + navLink.getClassUrl());
                // 获取软件列表信息
                List<SoftwareInfo> softwareList = getSoftwareList(softDoc);
                System.out.println("软件列表信息: " + softwareList);
                softwareList.forEach(softwareInfo -> {
                    classMappings.add(SoftwareClassMapping.builder()
                            .softwareId(softwareInfo.getSoftwareId())
                            .classId(navLink.getId()).build());
                });
                allSoftwareList.addAll(softwareList);
            }
            List<SoftwareInfo> deduplicatedList = new ArrayList<>(allSoftwareList.stream()
                    // 转Map：key=softwareId，value=SoftwareInfo，重复key时取第一个
                    .collect(Collectors.toMap(
                            SoftwareInfo::getSoftwareId, // 以softwareId为Key
                            info -> info,                // 以自身为Value
                            (existing, replacement) -> existing // 重复时保留原有元素
                    ))
                    .values()); // 转回List
            softwareInfoMapper.insert(deduplicatedList);
            softwareClassMappingMapper.insert(classMappings);
            deduplicatedList.forEach(softwareInfo -> {

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Document buildDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
    }

    /**
     * 获取导航分类下的所有链接
     * 选择器：.nav-cat-list-new 下的每个 li 标签内的 a 标签
     */

    public List<SoftwareClass> getNavCategoryLinks(Document doc) {
        List<SoftwareClass> navLinks = new ArrayList<>();

        // 定位到导航分类列表
        Elements navItems = doc.select(".nav-cat-list-new li.nav-cat-item-new");

        for (Element item : navItems) {
            Element link = item.select("a.nav-cat-a-new").first();
            if (link != null) {
                SoftwareClass linkInfo = new SoftwareClass();

                // 获取href属性
                String href = link.attr("href");
                // 获取分类名称（从img的alt属性或文本内容）
                String name = link.text();

                linkInfo.setClassUrl(href);
                linkInfo.setClassName(name);

                navLinks.add(linkInfo);
            }
        }

        return navLinks;
    }

    /**
     * 获取软件列表信息
     * 获取每个 li 标签内：
     * 1. a标签的href（详情链接）
     * 2. a标签的title（软件名称）
     * 3. img标签的src（图标地址）
     */

    public List<SoftwareInfo> getSoftwareList(Document doc) {
        List<SoftwareInfo> softwareList = new ArrayList<>();

        // 定位到软件列表
        Elements softwareItems = doc.select("ul.category-list li.category-item");

        for (Element item : softwareItems) {
            SoftwareInfo softwareInfo = new SoftwareInfo();

            // 获取详情链接（a标签的href）
            Element detailLink = item.select("a.category-info").first();
            if (detailLink != null) {
                String detailUrl = detailLink.attr("href");
                String title = detailLink.attr("title");

                softwareInfo.setDetailUrl(detailUrl);
                softwareInfo.setSoftwareName(title);
            }

            // 下载链接（立即下载）
            Element fastDownload = item.select("a.J_qq_download").first();
            if (fastDownload != null) {
                softwareInfo.setSoftwareId(fastDownload.attr("data-id"));
            }

            // 获取图标地址（img标签的src）
            Element iconImg = item.select("img").first();
            if (iconImg != null) {
                String iconUrl = iconImg.attr("src");
                softwareInfo.setLogoUrl(iconUrl);
            }
            // 评分信息
            Element starElement = item.select("i.star").first();
            if (starElement != null) {
                String starWidth = starElement.attr("style");
                softwareInfo.setRating(starWidth);
            }


            // 只有当至少获取到一个信息时才添加
            softwareList.add(softwareInfo);
        }

        return softwareList;
    }

    /**
     * 扩展方法：获取完整的软件信息（包括下载链接等）
     */

    public List<Map<String, Object>> getFullSoftwareInfo(Document doc) {
        List<Map<String, Object>> fullInfoList = new ArrayList<>();

        Elements softwareItems = doc.select("ul.category-list li.category-item");

        for (Element item : softwareItems) {
            Map<String, Object> info = new HashMap<>();

            // 基本信息
            Element detailLink = item.select("a.category-info").first();
            if (detailLink != null) {
                info.put("detailUrl", detailLink.attr("href"));
                info.put("title", detailLink.attr("title"));
            }

            // 图标
            Element iconImg = item.select("img").first();
            if (iconImg != null) {
                info.put("iconUrl", iconImg.attr("src"));
            }

            // 下载链接（立即下载）
            Element fastDownload = item.select("a.J_qq_download").first();
            if (fastDownload != null) {
                info.put("fastDownloadTag", fastDownload.attr("data-hottag"));
                info.put("softwareId", fastDownload.attr("data-id"));
            }

            // 评分信息
            Element starElement = item.select("i.star").first();
            if (starElement != null) {
                String starWidth = starElement.attr("style");
                info.put("rating", starWidth);
            }

            fullInfoList.add(info);
        }

        return fullInfoList;
    }
}
