package com.gd.self.tool.tx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author guodun
 * @date 2026/3/18 11:20
 * @description 分类信息
 */
@Data
@TableName("tx_category_info")
public class CategoryInfo {
    //分类
    @TableId(type = IdType.AUTO)
    private Long id;
    private String categoryName;
    private String categoryUrl;
    private String categoryId;
    private String totalCount;
    private String iconUrl;
    private boolean isCurrent;
}
