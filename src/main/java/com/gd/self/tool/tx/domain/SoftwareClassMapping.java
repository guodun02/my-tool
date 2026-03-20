package com.gd.self.tool.tx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * @author guodun
 * @date 2026/3/19 13:55
 * @description 分类软件映射
 */
@TableName("tx_software_class_mapping")
@Data
@Builder
public class SoftwareClassMapping {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private Long classId;
    private String softwareId;
}
