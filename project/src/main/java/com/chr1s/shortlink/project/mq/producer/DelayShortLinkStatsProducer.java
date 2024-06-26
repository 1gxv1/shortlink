package com.chr1s.shortlink.project.mq.producer;

import com.chr1s.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.chr1s.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissonClient;

    public void send(ShortLinkStatsRecordDTO shortLinkStatsRecordDTO) {
        RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
        RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        delayedQueue.offer(shortLinkStatsRecordDTO, 5, TimeUnit.SECONDS);
    }
}
