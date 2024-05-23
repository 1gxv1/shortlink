package com.chr1s.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chr1s.shortlink.project.dao.entity.LinkAccessStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDTO> {

    @Insert("INSERT INTO t_link_access_stats(full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)" +
            "VALUES (#{linkAccessStats.fullShortUrl},#{linkAccessStats.gid},#{linkAccessStats.date},#{linkAccessStats.pv},#{linkAccessStats.uv},#{linkAccessStats.uip},#{linkAccessStats.hour},#{linkAccessStats.weekday},NOW(),NOW(),0)ON DUPLICATE KEY UPDATE pv=pv+1," +
            "uv=uv+#{linkAccessStats.uv}," +
            "uip=uip+#{linkAccessStats.uip};")
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDTO linkAccessStatsDTO);
}
