package com.chr1s.shortlink.admin.controller;

import com.chr1s.shortlink.admin.common.convention.result.Result;
import com.chr1s.shortlink.admin.common.convention.result.Results;
import com.chr1s.shortlink.admin.dto.req.GroupSaveReqDTO;
import com.chr1s.shortlink.admin.dto.req.GroupUpdateReqDTO;
import com.chr1s.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.chr1s.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/api/short-link/v1/group")
    public Result<Void> add(@RequestBody GroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getGroupName());
        return Results.success();
    }

    @GetMapping("/api/short-link/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    @PutMapping("/api/short-link/v1/group")
    public Result<Void> updateGroup(@RequestBody GroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }
}
