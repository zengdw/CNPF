package com.zengd.cnpf.vo;

/**
 * @author zengd
 */
public record WheelTask(long execMilli,
                        long period,
                        Runnable mission) {
}
