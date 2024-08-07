package com.zengd.cnpf.protocol;

import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.handler.Channel;
import com.zengd.cnpf.lock.State;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.MemorySegment;

/**
 * @author zengd
 * @date 2024/7/19 下午4:52
 */
public final class SentryPollerNode implements PollerNode {
    private final Logger log = LoggerFactory.getLogger(SentryPollerNode.class);

    private final IntMap<PollerNode> nodeMap;

    private final Channel channel;

    private final Sentry sentry;

    private final State channelState = new State(Constants.NET_W);

    private final Runnable callback;

    public SentryPollerNode(IntMap<PollerNode> nodeMap, Channel channel, Sentry sentry, Runnable callback) {
        this.nodeMap = nodeMap;
        this.channel = channel;
        this.sentry = sentry;
        this.callback = callback;
    }

    @Override
    public void onReadableEvent(MemorySegment reserved, int len) {
        try {
            handleEvent(sentry.onReadableEvent(reserved, len));
        } catch (FrameworkException e) {
            log.error("Exception thrown in sentryPollerNode when invoking onReadableEvent()", e);
            close();
        }
    }

    @Override
    public void onWritableEvent() {
        try {
            handleEvent(sentry.onWritableEvent());
        } catch (FrameworkException e) {
            log.error("Exception thrown in sentryPollerNode when invoking onWritableEvent()", e);
            close();
        }
    }

    private void handleEvent(int r) {
        if (r == Constants.NET_UPDATE) {
            updateToProtocol();
        } else if (r == Constants.NET_R || r == Constants.NET_W || r == Constants.NET_RW) {
            ctl(r);
        } else if (r != Constants.NET_IGNORED) {
            throw new RuntimeException(Constants.UNREACHED);
        }
    }

    private void ctl(int expected) {
        // TODO 修改多路复用的状态
    }

    private void updateToProtocol() {
        // TODO 升级为Protocol
    }

    private void close() {
        if (nodeMap.remove(channel.socket().intValue(), this)) {
            closeSentry();
        }
    }

    private void closeSentry() {
        try {
            sentry.doClose();
        } catch (RuntimeException e) {
            log.error("Failed to close sentry", e);
        }
        if (callback != null) {
            Thread.ofVirtual().start(callback);
        }
    }
}
