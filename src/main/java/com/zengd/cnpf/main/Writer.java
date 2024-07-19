package com.zengd.cnpf.main;

import com.zengd.cnpf.config.WriterConfig;
import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.vo.WriterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengd
 * @date 2024/7/18 下午3:00
 */
public final class Writer {
    private static final Logger log = LoggerFactory.getLogger(Writer.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final Thread writerThread;

    private final Queue<WriterTask> queue = new LinkedTransferQueue<>();

    public Writer(WriterConfig writerConfig) {
        this.writerThread = createWriterThread(writerConfig);
    }

    public Thread thread() {
        return writerThread;
    }

    public void submit(WriterTask writerTask) {
        if (writerTask == null || !queue.offer(writerTask)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    private Thread createWriterThread(WriterConfig writerConfig) {
        int sequence = COUNTER.getAndIncrement();
        return Thread.ofPlatform().name("writer-" + sequence).unstarted(() -> {
            // 队列事件处理
        });
    }

}
