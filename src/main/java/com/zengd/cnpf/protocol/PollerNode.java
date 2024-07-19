package com.zengd.cnpf.protocol;

import java.lang.foreign.MemorySegment;

/**
 * @author zengd
 * @date 2024/7/19 下午4:52
 */
public sealed interface PollerNode permits SentryPollerNode, ProtocolPollerNode {
    void onReadableEvent(MemorySegment reserved, int len);

    void onWritableEvent();
}
