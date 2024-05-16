package com.chr1s.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chr1s.shortlink.admin.dao.entity.GroupDo;
import com.chr1s.shortlink.admin.dto.req.GroupSortReqDTO;
import com.chr1s.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.chr1s.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDo> {
    void saveGroup(String groupName);

    List<ShortLinkGroupRespDTO> listGroup();

    void updateGroup(GroupUpdateReqDTO requestParam);

    void deleteGroup(String gid);

    void sortGroup(List<GroupSortReqDTO> requestParam);
}
