package com.zengd.cnpf.main;

import com.zengd.cnpf.config.ListenerConfig;
import com.zengd.cnpf.config.NetConfig;
import com.zengd.cnpf.config.PollerConfig;
import com.zengd.cnpf.config.WriterConfig;
import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.lock.Mutex;
import com.zengd.cnpf.lock.State;
import com.zengd.cnpf.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * 框架启动类
 *
 * @author zengd
 * @date 2024/7/18 下午2:42
 */
public final class Net extends AbstractLifeCycle {

    private static final Logger log = LoggerFactory.getLogger(Net.class);

    private static final AtomicBoolean INSTANCE_FLAG = new AtomicBoolean(false);

    private final State state = new State();
    private final List<Poller> pollers;
    private final List<Writer> writers;
    private final Thread netThread;

    public Net(NetConfig netConfig, PollerConfig pollerConfig, WriterConfig writerConfig) {
        if (netConfig == null || pollerConfig == null || writerConfig == null) {
            throw new NullPointerException();
        }
        if (!INSTANCE_FLAG.compareAndSet(false, true)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
        int pollerCount = pollerConfig.getPollerCount();
        if (pollerCount <= 0) {
            throw new FrameworkException(ExceptionType.NETWORK, "Poller instances cannot be zero");
        }
        int writerCount = writerConfig.getWriterCount();
        if (writerCount <= 0) {
            throw new FrameworkException(ExceptionType.NETWORK, "Writer instances cannot be zero");
        }
        this.pollers = IntStream.range(0, pollerCount).mapToObj(i -> new Poller(pollerConfig)).toList();
        this.writers = IntStream.range(0, writerCount).mapToObj(i -> new Writer(writerConfig)).toList();
        this.netThread = createNetThread(netConfig);
    }

    private Thread createNetThread(NetConfig netConfig) {
        return Thread.ofPlatform().unstarted(() -> {
            // 创建主线程
        });
    }

    public void addListener(ListenerConfig listenerConfig) {
        try (Mutex mutex = state.withMutex()) {
            int current = state.get();
            if (current > Constants.RUNNING) {
                throw new RuntimeException(Constants.UNREACHED);
            }
            // 添加Listener的事件注册逻辑
        }
    }

    @Override
    protected void doInit() {
        try (Mutex mutex = state.withMutex()) {
            // 初始化
        }
    }

    @Override
    protected void doExit() throws InterruptedException {
        try (Mutex mutex = state.withMutex()) {
            // 释放
        }
    }
}
