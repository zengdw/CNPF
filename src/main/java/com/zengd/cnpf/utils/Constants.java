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
}
