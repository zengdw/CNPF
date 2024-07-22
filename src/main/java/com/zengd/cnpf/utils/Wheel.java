package com.zengd.cnpf.utils;

import com.zengd.cnpf.main.LifeCycle;

import java.time.Duration;

/**
 * @author zengd
 * @date 2024/7/22 下午5:32
 */
public sealed interface Wheel extends LifeCycle permits WheelImpl {
    int slots = Integer.getInteger("wheel.slots", 4096);
    long tick = Long.getLong("wheel.tick", 10L);

    static Wheel wheel() {
        return WheelImpl.INSTANCE;
    }

    Runnable addJob(Runnable mission, Duration delay);

    Runnable addPeriodicJob(Runnable mission, Duration delay, Duration period);
}
