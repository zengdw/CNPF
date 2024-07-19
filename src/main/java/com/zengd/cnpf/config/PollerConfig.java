package com.zengd.cnpf.config;

import com.zengd.cnpf.utils.NativeUtil;

/**
 * @author zengd
 * @date 2024/7/18 下午3:00
 */
public final class PollerConfig {
    private int pollerCount = Math.max(NativeUtil.getCpuCores() >> 1, 4);

    public int getPollerCount() {
        return pollerCount;
    }

    public void setPollerCount(int pollerCount) {
        this.pollerCount = pollerCount;
    }
}
