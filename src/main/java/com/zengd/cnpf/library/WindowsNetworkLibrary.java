package com.zengd.cnpf.library;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

/**
 * @author zengd
 * @date 2024/7/18 下午4:05
 */
public final class WindowsNetworkLibrary implements OsNetworkLibrary {
    private static final MemoryLayout EPOLL_DATA_LAYOUT = MemoryLayout.unionLayout(
            ValueLayout.ADDRESS.withName("ptr"),
            ValueLayout.JAVA_INT.withName("fd"),
            ValueLayout.JAVA_INT.withName("u32"),
            ValueLayout.JAVA_LONG.withName("u64"),
            ValueLayout.JAVA_INT.withName("sock"),
            ValueLayout.ADDRESS.withName("hnd")
    );

    private static final MemoryLayout EPOLL_EVENT_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("events"),
            MemoryLayout.paddingLayout(4),
            EPOLL_DATA_LAYOUT.withName("data")
    );
}
