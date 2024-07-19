package com.zengd.cnpf.encode;

import com.zengd.cnpf.buffer.WriteBuffer;

/**
 * @author zengd
 * @date 2024/7/19 下午4:25
 */
@FunctionalInterface
public interface Encoder {
    void encode(WriteBuffer writeBuffer, Object o);
}
