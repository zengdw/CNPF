package com.zengd.cnpf.config;

import com.zengd.cnpf.utils.NativeUtil;

/**
 * @author zengd
 * @date 2024/7/18 下午3:01
 */
public final class WriterConfig {
    private int writerCount = Math.max(NativeUtil.getCpuCores() >> 1, 4);

    public int getWriterCount() {
        return writerCount;
    }

    public void setWriterCount(int writerCount) {
        this.writerCount = writerCount;
    }
}
