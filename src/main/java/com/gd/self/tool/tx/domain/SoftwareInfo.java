package com.gd.self.tool.tx.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author guodun
 * @date 2026/3/18 11:07
 * @description 软件信息
 */
@Data
@TableName("tx_software_info")
public class SoftwareInfo {
    @TableId(type = IdType.INPUT)
    private String softwareId;
    private String detailUrl;
    private String softwareName;
    private String logoUrl;
    private String size;
    private String operatingSystem;
    private String version;
    private LocalDateTime updateTime;
    private String description;
    @TableField(exist = false)
    private List<SoftwareTag> tags;
    //轮播图
    @TableField(exist = false)
    private List<String> sliderImageList;
    private String sliderImages;
    private String rating;

    public String getSliderImages() {
        if (CollUtil.isEmpty(sliderImageList)){
            return "";
        }
        return StrUtil.join(",", sliderImageList);
    }
}
