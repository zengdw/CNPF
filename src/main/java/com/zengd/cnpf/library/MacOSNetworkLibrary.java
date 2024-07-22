package com.zengd.cnpf.library;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

/**
 * @author zengd
 * @date 2024/7/18 下午4:06
 */
public final class MacOSNetworkLibrary implements OsNetworkLibrary {
    private static final MemoryLayout keventLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG_UNALIGNED.withName("ident"),
            ValueLayout.JAVA_SHORT_UNALIGNED.withName("filter"),
            ValueLayout.JAVA_SHORT_UNALIGNED.withName("flags"),
            ValueLayout.JAVA_INT_UNALIGNED.withName("fflags"),
            ValueLayout.JAVA_LONG_UNALIGNED.withName("data"),
            ValueLayout.ADDRESS_UNALIGNED.withName("udata")
    ).withByteAlignment(4L);
}
