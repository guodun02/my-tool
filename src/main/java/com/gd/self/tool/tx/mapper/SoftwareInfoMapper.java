package com.gd.self.tool.tx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gd.self.tool.tx.domain.SoftwareInfo;

/**
 * @author guodun
 * @date 2026/3/19 13:52
 * @description 软件
 */
public interface SoftwareInfoMapper extends BaseMapper<SoftwareInfo> {
    Long countAll();
}

