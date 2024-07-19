package com.zengd.cnpf.handler;

import com.zengd.cnpf.encode.Decoder;
import com.zengd.cnpf.encode.Encoder;
import com.zengd.cnpf.main.Poller;
import com.zengd.cnpf.main.Writer;
import com.zengd.cnpf.vo.Loc;
import com.zengd.cnpf.vo.Socket;

/**
 * 一个channel对象代表一个成功建立的TCP连接
 *
 * @author zengd
 * @date 2024/7/19 下午4:34
 */
public sealed interface Channel permits ChannelImpl {
    Socket socket();

    Encoder encoder();

    Decoder decoder();

    Handler handler();

    Poller poller();

    Writer writer();

    Loc loc();
}
