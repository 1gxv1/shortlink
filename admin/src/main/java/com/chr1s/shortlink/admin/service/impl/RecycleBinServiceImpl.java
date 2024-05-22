package com.chr1s.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chr1s.shortlink.admin.common.biz.user.UserContext;
import com.chr1s.shortlink.admin.common.convention.exception.ClientException;
import com.chr1s.shortlink.admin.common.convention.exception.ServiceException;
import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.dao.entity.GroupDo;
import com.chr1s.shortlink.admin.dao.mapper.GroupMapper;
import com.chr1s.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.chr1s.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {
    //    修改为远程调用
    private final GroupMapper groupMapper;

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getDelFlag, 0);
        List<GroupDo> groupDos = groupMapper.selectList(queryWrapper);
        if (groupDos.isEmpty()) {
            throw new ServiceException("用户无分组信息！");
        }
        requestParam.setGidList(groupDos.stream().map(GroupDo::getGid).toList());
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);

    }
}
