package com.zengd.cnpf.protocol;

import java.lang.foreign.MemorySegment;

/**
 * @author zengd
 * @date 2024/7/19 下午4:52
 */
public final class SentryPollerNode implements PollerNode {
    @Override
    public void onReadableEvent(MemorySegment reserved, int len) {

    }

    @Override
    public void onWritableEvent() {

    }
}
