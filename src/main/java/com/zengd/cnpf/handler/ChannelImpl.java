package com.zengd.cnpf.handler;

import com.zengd.cnpf.encode.Decoder;
import com.zengd.cnpf.encode.Encoder;
import com.zengd.cnpf.main.Poller;
import com.zengd.cnpf.main.Writer;
import com.zengd.cnpf.vo.Loc;
import com.zengd.cnpf.vo.Socket;

/**
 * @author zengd
 * @date 2024/7/19 下午4:35
 */
public final class ChannelImpl implements Channel {
    @Override
    public Socket socket() {
        return null;
    }

    @Override
    public Encoder encoder() {
        return null;
    }

    @Override
    public Decoder decoder() {
        return null;
    }

    @Override
    public Handler handler() {
        return null;
    }

    @Override
    public Poller poller() {
        return null;
    }

    @Override
    public Writer writer() {
        return null;
    }

    @Override
    public Loc loc() {
        return null;
    }
}
