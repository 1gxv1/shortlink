package com.chr1s.shortlink.project.mq.consumer;

import com.chr1s.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.chr1s.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static com.chr1s.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;

    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }

    private void onMessage() {

        Executors.newSingleThreadExecutor(
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("delay_short-link_stats_consumer");
                    return thread;
                }
        ).execute(() -> {
//            创建阻塞队列，在队列有东西的时候可以被线程拿出来执行，在没有东西的时候会阻塞
            RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
//            创建延迟队列，首先将元素放入延迟队列当中，只有达到该延迟时间后，才会将元素放到阻塞队列进而被消费
            RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            while (true) {
                try {
                    ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                    if (statsRecord != null) {
                        shortLinkService.shortLinkStats(null, null, statsRecord);
                        continue;
                    }
//                    让线程睡眠，防止无限消耗资源
                    LockSupport.parkUntil(500);
                } catch (Throwable ignored) {

                }

            }
        });

    }
}
