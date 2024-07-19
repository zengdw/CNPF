package com.zengd.cnpf.vo;

import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;

public record Loc(IpType ipType, String ip, int port) {
    private static final int PORT_MAX = 65535;

    public short shortPort() {
        if (port < 0 || port > PORT_MAX) {
            throw new FrameworkException(ExceptionType.NETWORK, "Port number overflow");
        }
        return (short) port;
    }

    @Override
    public String toString() {
        return STR."[\{ip == null || ip.isBlank() ? "localhost" : ip}:\{port}]";
    }

}
