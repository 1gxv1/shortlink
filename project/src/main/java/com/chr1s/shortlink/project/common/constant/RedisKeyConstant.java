package com.chr1s.shortlink.project.common.constant;

public class RedisKeyConstant {


    public static final String GOTO_SHORT_LINK_KEY = "short_link_goto_%s";

    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short_link_lock_goto_%s";

    public static final String COOKIE_STATS_SHORT_LINK_KEY = "short-link_stats_uv_%s";

    public static final String IP_STATS_SHORT_LINK_KEY = "short-link_stats_ip_%s";

    /**
     * 短链接修改分组 ID 锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link_lock_update-gid_%s";

    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "short-link_delay-queue:stats";

}
