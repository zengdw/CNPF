package com.zengd.cnpf.protocol;

import java.lang.foreign.MemorySegment;

/**
 * 第二阶段
 *
 * @author zengd
 * @date 2024/7/19 下午4:51
 */
public interface Protocol {
    int onReadableEvent(MemorySegment reserved, int len);

    int onWritableEvent();

    int doWrite(MemorySegment data, int len);

    void doShutdown();

    void doClose();
}
