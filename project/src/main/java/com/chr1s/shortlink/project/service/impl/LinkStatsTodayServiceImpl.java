package com.chr1s.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chr1s.shortlink.project.dao.entity.LinkStatsTodayDO;
import com.chr1s.shortlink.project.dao.mapper.LinkStatsTodayMapper;
import com.chr1s.shortlink.project.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}
