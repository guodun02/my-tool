package com.gd.self.tool.tx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author guodun
 * @date 2026/3/18 11:08
 * @description 软件标签信息
 */
@Data
@TableName("tx_software_tag")
public class SoftwareTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String softwareId;
    private String tagUrl;
    private String tagName;
}
