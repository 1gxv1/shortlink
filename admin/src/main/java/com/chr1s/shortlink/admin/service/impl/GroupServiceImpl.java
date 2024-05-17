package com.chr1s.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chr1s.shortlink.admin.common.biz.user.UserContext;
import com.chr1s.shortlink.admin.common.convention.exception.ClientException;
import com.chr1s.shortlink.admin.dao.entity.GroupDo;
import com.chr1s.shortlink.admin.dao.mapper.GroupMapper;
import com.chr1s.shortlink.admin.dto.req.GroupSortReqDTO;
import com.chr1s.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.chr1s.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.chr1s.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.chr1s.shortlink.admin.remote.dto.resp.ShortLinkGroupCountRespDTO;
import com.chr1s.shortlink.admin.service.GroupService;
import com.chr1s.shortlink.admin.util.RandomGenerator;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDo> implements GroupService {
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };
    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomGenerator.generateRandomString();
        } while (hasGrid(gid));
        GroupDo build = GroupDo.builder()
                .name(groupName)
                .username(UserContext.getUsername())
                .sortOrder(0)
                .gid(RandomGenerator.generateRandomString())
                .build();
        baseMapper.insert(build);
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .orderByAsc(GroupDo::getSortOrder)
                .orderByAsc(GroupDo::getUpdateTime);
        List<GroupDo> groups = baseMapper.selectList(queryWrapper);
        List<ShortLinkGroupCountRespDTO> gids = shortLinkRemoteService.listGroupLinkCount(groups.stream().map(GroupDo::getGid).toList()).getData();
        List<ShortLinkGroupRespDTO> results = BeanUtil.copyToList(groups, ShortLinkGroupRespDTO.class);
        Map<String, Integer> counts = gids.stream().collect(Collectors.toMap(ShortLinkGroupCountRespDTO::getGid, ShortLinkGroupCountRespDTO::getShortLinkCount));
        return results.stream().peek(result -> result.setShortLinkCount(counts.get(result.getGid()))).toList();
    }

    @Override
    public void updateGroup(GroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDo> eq = Wrappers.lambdaUpdate(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getGid, requestParam.getGid())
                .eq(GroupDo::getDelFlag, 0);
        GroupDo groupDo = new GroupDo();
        groupDo.setName(requestParam.getName());
        int update = baseMapper.update(groupDo, eq);
        if (update < 1) throw new ClientException("更新组名失败，数据库错误");
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDo> eq = Wrappers.lambdaUpdate(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getDelFlag, 0);
        System.out.println(UserContext.getUsername());
        GroupDo groupDo = new GroupDo();
        groupDo.setDelFlag(1);
        GroupDo groupDo1 = baseMapper.selectOne(eq);
        int update = baseMapper.update(groupDo, eq);
        if (update < 1) throw new ClientException("更新组名失败，数据库错误");
    }

    @Override
    public void sortGroup(List<GroupSortReqDTO> requestParam) {
        for (GroupSortReqDTO groupSortReqDTO : requestParam) {
            GroupDo groupDo = GroupDo.builder()
                    .sortOrder(groupSortReqDTO.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDo> eq = Wrappers.lambdaUpdate(GroupDo.class)
                    .eq(GroupDo::getUsername, UserContext.getUsername())
                    .eq(GroupDo::getGid, groupSortReqDTO.getGid())
                    .eq(GroupDo::getDelFlag, 0);
            baseMapper.update(groupDo, eq);
        }

    }

    private boolean hasGrid(String gid) {
        LambdaQueryWrapper<GroupDo> wrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getUsername, UserContext.getUsername());
        GroupDo selectedOne = baseMapper.selectOne(wrapper);
        return selectedOne != null;
    }
}
