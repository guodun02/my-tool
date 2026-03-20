package com.gd.self.tool.tx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author guodun
 * @date 2026/3/18 11:34
 * @description 分类
 */
@Data
@TableName("tx_software_class")
public class SoftwareClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private String classUrl;
}
