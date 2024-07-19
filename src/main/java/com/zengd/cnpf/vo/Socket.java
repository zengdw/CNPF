package com.zengd.cnpf.vo;

import com.zengd.cnpf.utils.NativeUtil;

/**
 * @author zengd
 */
public record Socket(long longValue,
                     int intValue) {
    public Socket(int socket) {
        this(socket, socket);
    }

    public Socket(long socket) {
        this(socket, NativeUtil.castInt(socket));
    }

    @Override
    public int hashCode() {
        return intValue;
    }
}
