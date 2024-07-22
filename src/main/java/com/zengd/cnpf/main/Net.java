package com.zengd.cnpf.main;

import com.zengd.cnpf.config.*;
import com.zengd.cnpf.encode.Decoder;
import com.zengd.cnpf.encode.Encoder;
import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.handler.Channel;
import com.zengd.cnpf.handler.ChannelImpl;
import com.zengd.cnpf.handler.Handler;
import com.zengd.cnpf.library.OsNetworkLibrary;
import com.zengd.cnpf.lock.Mutex;
import com.zengd.cnpf.lock.State;
import com.zengd.cnpf.protocol.Provider;
import com.zengd.cnpf.protocol.Sentry;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.Wheel;
import com.zengd.cnpf.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 框架启动类
 *
 * @author zengd
 * @date 2024/7/18 下午2:42
 */
public final class Net extends AbstractLifeCycle {

    private static final Logger log = LoggerFactory.getLogger(Net.class);
    private static final SocketConfig defaultSocketConfig = new SocketConfig();
    private static final DurationWithCallback defaultDurationWithCallback = new DurationWithCallback(Duration.ofSeconds(5), null);
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final AtomicBoolean INSTANCE_FLAG = new AtomicBoolean(false);
    private final OsNetworkLibrary osNetworkLibrary = OsNetworkLibrary.CURRENT;
    private final State state = new State();
    private final List<Poller> pollers;
    private final List<Writer> writers;
    private final Thread netThread;

    public void addProvider(Provider provider) {
        try (Mutex _ = state.withMutex()) {
            // TODO 将provider注册到当前net中
        }
    }

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
        this.pollers = IntStream.range(0, pollerCount).mapToObj(_ -> new Poller(pollerConfig)).toList();
        this.writers = IntStream.range(0, writerCount).mapToObj(_ -> new Writer(writerConfig)).toList();
        this.netThread = createNetThread(netConfig);
    }

    public void connect(Loc loc, Encoder encoder, Decoder decoder, Handler handler, Provider provider, SocketConfig socketConfig, DurationWithCallback durationWithCallback) {
        Socket socket = osNetworkLibrary.createSocket(loc);
        osNetworkLibrary.configureClientSocket(socket, socketConfig);
        int seq = counter.getAndIncrement();
        Poller poller = pollers.get(seq % pollers.size());
        Writer writer = writers.get(seq % writers.size());
        Channel channel = new ChannelImpl(socket, encoder, decoder, handler, poller, writer, loc);
        addProvider(provider);
        Sentry sentry = provider.create(channel);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment sockAddr = osNetworkLibrary.createSockAddr(loc, arena);
            int r = osNetworkLibrary.connect(socket, sockAddr);
            if (r == 0) {
                poller.submit(new PollerTask(PollerTaskType.BIND, channel, sentry));
                osNetworkLibrary.ctlMux(poller.mux(), socket, Constants.NET_NONE, Constants.NET_W);
            } else if (r < 0) {
                int errno = Math.abs(r);
                if (errno == osNetworkLibrary.connectBlockCode()) {
                    Duration duration = durationWithCallback.duration();
                    Runnable callback = durationWithCallback.callback();
                    poller.submit(new PollerTask(PollerTaskType.BIND, channel, callback == null ? sentry : new SentryWithCallback(sentry, callback)));
                    Wheel.wheel().addJob(() -> poller.submit(new PollerTask(PollerTaskType.UNBIND, channel, null)), duration);
                    osNetworkLibrary.ctlMux(poller.mux(), socket, Constants.NET_NONE, Constants.NET_W);
                } else {
                    throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to connect, errno : \{errno}");
                }
            } else {
                throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
            }
        }
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
