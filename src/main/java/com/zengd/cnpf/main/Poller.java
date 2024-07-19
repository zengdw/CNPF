package com.zengd.cnpf.main;

import com.zengd.cnpf.config.PollerConfig;
import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.vo.PollerTask;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengd
 * @date 2024/7/18 下午3:00
 */
public final class Poller {
    private static final Logger log = LoggerFactory.getLogger(Poller.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final Thread pollerThread;

    private final Queue<PollerTask> readerTaskQueue = new MpscUnboundedAtomicArrayQueue<>(1024);

    public Poller(PollerConfig pollerConfig) {
        this.pollerThread = createPollerThread(pollerConfig);
    }

    public Thread thread() {
        return pollerThread;
    }

    public void submit(PollerTask pollerTask) {
        if (pollerTask == null || !readerTaskQueue.offer(pollerTask)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    private Thread createPollerThread(PollerConfig pollerConfig) {
        int sequence = COUNTER.getAndIncrement();
        return Thread.ofPlatform().name("poller-" + sequence).unstarted(() -> {
            for (; ; ) {
                // 多路复用监听

                // 队列事件处理
            }
        });
    }

}
