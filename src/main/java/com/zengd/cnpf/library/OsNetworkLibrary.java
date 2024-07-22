package com.zengd.cnpf.library;

import com.zengd.cnpf.config.SocketConfig;
import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.NativeUtil;
import com.zengd.cnpf.vo.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author zengd
 * @date 2024/7/18 下午4:04
 */
public sealed interface OsNetworkLibrary permits WindowsNetworkLibrary, LinuxNetworkLibrary, MacOSNetworkLibrary {
    String IPV4_MAPPED_FORMAT = "::ffff:";
    int IPV4_PREFIX_LENGTH = IPV4_MAPPED_FORMAT.length();

    OsNetworkLibrary CURRENT = switch (NativeUtil.ostype()) {
        case OsType.Windows -> new WindowsNetworkLibrary();
        case OsType.Linux -> new LinuxNetworkLibrary();
        case OsType.MacOS -> new MacOSNetworkLibrary();
        default -> throw new FrameworkException(ExceptionType.NETWORK, "Unsupported operating system");
    };

    default int check(int value, String errMsg) {
        if (value < 0) {
            int errno = Math.abs(value);
            throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to \{errMsg} with err code : \{errno}");
        }
        return value;
    }

    default MemorySegment createSockAddr(Loc loc, Arena arena) {
        if (loc.ipType() == IpType.IPV4) {
            return createIpv4SockAddr(loc, arena);
        } else if (loc.ipType() == IpType.IPV6) {
            return createIpv6SockAddr(loc, arena);
        } else {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    private MemorySegment createIpv4SockAddr(Loc loc, Arena arena) {
        MemorySegment r = arena.allocate(ipv4AddressSize(), ipv4AddressAlign());
        MemorySegment ip = loc.ip() == null || loc.ip().isBlank() ? NativeUtil.NULL_POINTER : NativeUtil.allocateStr(arena, loc.ip(), ipv4AddressLen());
        if (check(setIpv4SockAddr(r, ip, loc.shortPort()), "set ipv4 address") == 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Ipv4 address is not valid : \{loc.ip()}");
        }
        return r;
    }

    private MemorySegment createIpv6SockAddr(Loc loc, Arena arena) {
        MemorySegment r = arena.allocate(ipv6AddressSize(), ipv6AddressAlign());
        MemorySegment ip = loc.ip() == null || loc.ip().isBlank() ? NativeUtil.NULL_POINTER : NativeUtil.allocateStr(arena, loc.ip(), ipv6AddressLen());
        if (check(setIpv6SockAddr(r, ip, loc.shortPort()), "set ipv6 address") == 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Ipv6 address is not valid : \{loc.ip()}");
        }
        return r;
    }

    default Socket createSocket(Loc loc) {
        return switch (loc.ipType()) {
            case IPV4 -> createIpv4Socket();
            case IPV6 -> createIpv6Socket();
            case null -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };
    }

    default void configureClientSocket(Socket socket, SocketConfig socketConfig) {
        check(setKeepAlive(socket, socketConfig.isKeepAlive()), "set client SO_REUSE_ADDR");
        check(setTcpNoDelay(socket, socketConfig.isTcpNoDelay()), "set client TCP_NODELAY");
        check(setNonBlocking(socket), "set client non-blocking");
    }

    default void configureServerSocket(Socket socket, Loc loc, SocketConfig socketConfig) {
        check(setReuseAddr(socket, socketConfig.isReuseAddr()), "set server SO_REUSE_ADDR");
        check(setKeepAlive(socket, socketConfig.isKeepAlive()), "set server SO_KEEPALIVE");
        check(setTcpNoDelay(socket, socketConfig.isTcpNoDelay()), "set server TCP_NODELAY");
        if (loc.ipType() == IpType.IPV6) {
            check(setIpv6Only(socket, socketConfig.isIpv6Only()), "set server IPV6_V6ONLY");
        }
        check(setNonBlocking(socket), "set server non-blocking");
    }

    default void bindAndListen(Socket socket, Loc loc, int backlog) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment addr = createSockAddr(loc, arena);
            checkInt(bind(socket, addr), "bind");
            checkInt(listen(socket, backlog), "listen");
        }
    }

    default SocketAndLoc accept(Loc loc, Socket socket, SocketConfig socketConfig) {
        return switch (loc.ipType()) {
            case IPV4 -> acceptIpv4Connection(socket, socketConfig);
            case IPV6 -> acceptIpv6Connection(socket, socketConfig);
            case null -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };
    }

    Mux createMux();

    MemoryLayout eventLayout();

    int muxWait(Mux mux, MemorySegment events, int maxEvents, Timeout timeout);

    IntPair access(MemorySegment events, int index);

    void ctl(Mux mux, Socket socket, int from, int to);

    int bind(Socket socket, MemorySegment addr);

    int listen(Socket socket, int backlog);

    int ipv4AddressLen();

    int ipv6AddressLen();

    int ipv4AddressSize();

    int ipv6AddressSize();

    int ipv4AddressAlign();

    int ipv6AddressAlign();

    int setIpv4SockAddr(MemorySegment sockAddr, MemorySegment address, short port);

    int setIpv6SockAddr(MemorySegment sockAddr, MemorySegment address, short port);

    short getIpv4Port(MemorySegment addr);

    short getIpv6Port(MemorySegment addr);

    int getIpv4Address(MemorySegment clientAddr, MemorySegment address);

    int getIpv6Address(MemorySegment clientAddr, MemorySegment address);

    int setReuseAddr(Socket socket, boolean b);

    int setKeepAlive(Socket socket, boolean b);

    int setTcpNoDelay(Socket socket, boolean b);

    int setIpv6Only(Socket socket, boolean b);

    int setNonBlocking(Socket socket);

    int getErrOpt(Socket socket);

    int connect(Socket socket, MemorySegment sockAddr);

    Socket accept(Socket socket, MemorySegment addr);

    int recv(Socket socket, MemorySegment data, int len);

    int send(Socket socket, MemorySegment data, int len);

    int connectBlockCode();

    int sendBlockCode();

    int interruptCode();

    int shutdownWrite(Socket socket);

    int closeSocket(Socket socket);

    void exitMux(Mux mux);

    void exit();

    private SocketAndLoc acceptIpv6Connection(Socket socket, SocketConfig socketConfig) {
        final int ipv6AddressLen = ipv6AddressLen();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment clientAddr = arena.allocate(ipv6AddressSize(), ipv6AddressAlign());
            MemorySegment address = arena.allocateArray(ValueLayout.JAVA_BYTE, ipv6AddressLen);
            Socket clientSocket = accept(socket, clientAddr);
            configureClientSocket(clientSocket, socketConfig);
            check(getIpv6Address(clientAddr, address), "get client's ipv6 address");
            String ip = NativeUtil.getStr(address, ipv6AddressLen);
            int port = 0xFFFF & getIpv6Port(clientAddr);
            if (ip.startsWith(IPV4_MAPPED_FORMAT)) {
                return new SocketAndLoc(clientSocket, new Loc(IpType.IPV4, ip.substring(IPV4_PREFIX_LENGTH), port));
            } else {
                return new SocketAndLoc(clientSocket, new Loc(IpType.IPV6, ip, port));
            }
        }
    }

    private SocketAndLoc acceptIpv4Connection(Socket socket, SocketConfig socketConfig) {
        final int ipv4AddressLen = ipv4AddressLen();
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment clientAddr = arena.allocate(ipv4AddressSize(), ipv4AddressAlign());
            MemorySegment address = arena.allocateArray(ValueLayout.JAVA_BYTE, ipv4AddressLen);
            Socket clientSocket = accept(socket, clientAddr);
            configureClientSocket(clientSocket, socketConfig);
            check(getIpv4Address(clientAddr, address), "get client's ipv4 address");
            String ip = NativeUtil.getStr(address, ipv4AddressLen);
            int port = 0xFFFF & getIpv4Port(clientAddr);
            Loc clientLoc = new Loc(IpType.IPV4, ip, port);
            return new SocketAndLoc(clientSocket, clientLoc);
        }
    }
}
