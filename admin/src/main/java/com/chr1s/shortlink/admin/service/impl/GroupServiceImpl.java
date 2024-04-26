package com.chr1s.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chr1s.shortlink.admin.dao.entity.GroupDo;
import com.chr1s.shortlink.admin.dao.mapper.GroupMapper;
import com.chr1s.shortlink.admin.service.GroupService;
import com.chr1s.shortlink.admin.util.RandomGenerator;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDo> implements GroupService {
    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomGenerator.generateRandomString();
        } while (hasGrid(gid));
        GroupDo build = GroupDo.builder()
                .name(groupName)
                .gid(RandomGenerator.generateRandomString())
                .build();
        baseMapper.insert(build);
    }

    private boolean hasGrid(String gid) {
        LambdaQueryWrapper<GroupDo> wrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getUsername, null);
        GroupDo selectedOne = baseMapper.selectOne(wrapper);
        return selectedOne != null;
    }
}
