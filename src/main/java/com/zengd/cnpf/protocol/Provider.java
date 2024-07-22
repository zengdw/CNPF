package com.zengd.cnpf.protocol;

import com.zengd.cnpf.handler.Channel;

/**
 * @author zengd
 * @date 2024/7/22 下午4:59
 */
@FunctionalInterface
public interface Provider {
    Sentry create(Channel channel);

    default void close() {

    }
}
