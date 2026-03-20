package com.gd.self.tool.tx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author guodun
 * @date 2026/3/18 11:22
 * @description 榜单
 */
@Data
@TableName("tx_ranking_software")
public class RankingSoftware {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String rankType;
    private String rank;
    private String softwareName;
    private String detailUrl;
    private String softwareId;
    private boolean hotTag;
}
