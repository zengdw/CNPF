package com.zengd.cnpf.protocol;

import java.lang.foreign.MemorySegment;

/**
 * 监听TCP连接的认证过程(第一阶段)
 *
 * @author zengd
 * @date 2024/7/19 下午4:51
 */
public interface Sentry {
    int onReadableEvent(MemorySegment reserved, int len);

    int onWritableEvent();

    Protocol toProtocol();

    void doClose();

}
