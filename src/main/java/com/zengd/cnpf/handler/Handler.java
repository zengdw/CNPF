package com.zengd.cnpf.handler;

/**
 * @author zengd
 * @date 2024/7/19 下午4:32
 */
public interface Handler {
    void onConnected(Channel channel);

    void onRecv(Channel channel, Object data);

    void onShutdown(Channel channel);

    void onRemoved(Channel channel);
}
