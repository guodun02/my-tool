package com.gd.self.tool.tx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author guodun
 * @date 2026/3/18 11:23
 * @description 推荐
 */
@Data
@TableName("tx_recommended_software")
public class RecommendedSoftware {
    @TableId(type = IdType.INPUT)
    private String softwareId;
    private String softwareName;
    private String detailUrl;
    private String iconUrl;
    private String description;
    private String downloadTag;
}
