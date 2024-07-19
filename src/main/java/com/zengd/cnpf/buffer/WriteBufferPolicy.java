package com.zengd.cnpf.buffer;

/**
 * 内存扩容策略
 *
 * @author zengd
 * @date 2024/7/18 下午1:51
 */
public interface WriteBufferPolicy {
    void resize(WriteBuffer writeBuffer, long nextIndex);

    void close(WriteBuffer writeBuffer);
}
