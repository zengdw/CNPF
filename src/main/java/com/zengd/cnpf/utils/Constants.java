package com.zengd.cnpf.utils;

/**
 * 常量类
 *
 * @author zengd
 * @date 2024/7/11 下午4:30
 */
public class Constants {
    /**
     * 表示在程序流程中，完全不应该触及的分支
     */
    public static final String UNREACHED = "Shouldn't be reached";
    /**
     * 表示C风格字符串的结尾标志
     */
    public static final byte NUT = (byte) '\0';

    public static final byte[] EMPTY_BYTES = new byte[0];

    //
    public static final int INITIAL = 0;

    public static final int STARTING = 1;

    public static final int RUNNING = 2;

    public static final int CLOSING = 3;

    public static final int STOPPED = 4;

    public static final int NET_NONE = Integer.MIN_VALUE;
    public static final int NET_IGNORED = Integer.MIN_VALUE | 1;
    public static final int NET_UPDATE = Integer.MIN_VALUE | (1 << 2);
    public static final int NET_W = Integer.MIN_VALUE | (1 << 4); // register write only
    public static final int NET_PW = Integer.MIN_VALUE | (1 << 6); // register write if possible
    public static final int NET_R = Integer.MIN_VALUE | (1 << 8); // register read only
    public static final int NET_PR = Integer.MIN_VALUE | (1 << 10); // register read if possible
    public static final int NET_RW = NET_R | NET_W; // register read and write
    public static final int NET_OTHER = Integer.MIN_VALUE | (1 << 20);

    public static final int EPOLL_IN = 1;
    public static final int EPOLL_OUT = 1 << 2;
    public static final int EPOLL_ERR = 1 << 3;
    public static final int EPOLL_HUP = 1 << 4;
    public static final int EPOLL_RDHUP = 1 << 13;

    public static final int EPOLL_CTL_ADD = 1;
    public static final int EPOLL_CTL_DEL = 2;
    public static final int EPOLL_CTL_MOD = 3;
}
