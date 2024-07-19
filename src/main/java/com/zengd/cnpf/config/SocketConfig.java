package com.zengd.cnpf.config;

/**
 * @author zengd
 * @date 2024/7/19 下午3:39
 */
public class SocketConfig {
    private boolean reuseAddr = true;

    private boolean keepAlive = false;

    private boolean tcpNoDelay = true;

    private boolean ipv6Only = false;

    public boolean isReuseAddr() {
        return reuseAddr;
    }

    public void setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isIpv6Only() {
        return ipv6Only;
    }

    public void setIpv6Only(boolean ipv6Only) {
        this.ipv6Only = ipv6Only;
    }
}
