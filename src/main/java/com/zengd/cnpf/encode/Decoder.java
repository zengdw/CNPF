package com.zengd.cnpf.encode;

import com.zengd.cnpf.buffer.ReadBuffer;

import java.util.List;

/**
 * @author zengd
 * @date 2024/7/19 下午4:25
 */
@FunctionalInterface
public interface Decoder {
    void decode(ReadBuffer readBuffer, List<Object> entityList);
}
