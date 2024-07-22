package com.zengd.cnpf.library;

import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.NativeUtil;
import com.zengd.cnpf.vo.IntPair;
import com.zengd.cnpf.vo.Mux;
import com.zengd.cnpf.vo.Socket;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author zengd
 * @date 2024/7/18 下午4:05
 */
public final class LinuxNetworkLibrary implements OsNetworkLibrary {
    private static final MemoryLayout EPOLL_DATA_LAYOUT = MemoryLayout.unionLayout(
            ValueLayout.ADDRESS_UNALIGNED.withName("ptr"),
            ValueLayout.JAVA_INT_UNALIGNED.withName("fd"),
            ValueLayout.JAVA_INT_UNALIGNED.withName("u32"),
            ValueLayout.JAVA_LONG_UNALIGNED.withName("u64")
    );
    private static final MemoryLayout EPOLL_EVENT_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT_UNALIGNED.withName("events"),
            EPOLL_DATA_LAYOUT.withName("data")
    );

    private static final long EVENT_SIZE = EPOLL_DATA_LAYOUT.byteSize();

    private static final long EVENTS_OFFSET = EPOLL_EVENT_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("events"));

    private static final long DATA_OFFSET = EPOLL_EVENT_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("data"));

    private static final long FD_OFFSET = EPOLL_EVENT_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("fd"));

    @Override
    public IntPair access(MemorySegment events, int index) {
        int event = NativeUtil.getInt(events, index * EVENT_SIZE + EVENTS_OFFSET);
        int socket = NativeUtil.getInt(events, index * EVENT_SIZE + DATA_OFFSET + FD_OFFSET);
        if ((event & (Constants.EPOLL_IN | Constants.EPOLL_RDHUP)) != 0) {
            return new IntPair(socket, Constants.NET_R);
        } else if ((event & Constants.EPOLL_OUT) != 0) {
            return new IntPair(socket, Constants.NET_W);
        } else if ((event & (Constants.EPOLL_ERR | Constants.EPOLL_HUP)) != 0) {
            return new IntPair(socket, Constants.NET_OTHER);
        } else {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    @Override
    public int ctl(Mux mux, Socket socket, int from, int to) {
        if (from == to) {
            return 0;
        }
        int epfd = mux.epfd();
        int fd = socket.intValue();
        if (to == Constants.NET_NONE) {
            return TenetLinuxBinding.epollCtl(epfd, Constants.EPOLL_CTL_DEL, fd, NativeUtil.NULL_POINTER);
        } else {
            int target = ((to & Constants.NET_R) != Constants.NET_NONE ? (Constants.EPOLL_IN | Constants.EPOLL_RDHUP) : 0) |
                    ((to & Constants.NET_W) != Constants.NET_NONE ? Constants.EPOLL_OUT : 0);
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment ev = arena.allocate(EPOLL_EVENT_LAYOUT);
                NativeUtil.setInt(ev, EVENTS_OFFSET, target);
                NativeUtil.setInt(ev, DATA_OFFSET + FD_OFFSET, fd);
                return TenetLinuxBinding.epollCtl(epfd, from == Constants.NET_NONE ? Constants.EPOLL_CTL_ADD : Constants.EPOLL_CTL_MOD, fd, ev);
            }
        }
    }

    @Override
    public MemoryLayout eventLayout() {
        return EPOLL_EVENT_LAYOUT;
    }

    @Override
    public int getErrOpt(Socket socket) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ptr = arena.allocate(ValueLayout.JAVA_INT, Integer.MIN_VALUE);
            checkInt(TenetLinuxBinding.getErrOpt(socket.intValue(), ptr), "get socket err opt");
            return NativeUtil.getInt(ptr, 0);
        }
    }
}
