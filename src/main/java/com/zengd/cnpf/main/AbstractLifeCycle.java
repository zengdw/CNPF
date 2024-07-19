package com.zengd.cnpf.main;

import com.zengd.cnpf.utils.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zengd
 * @date 2024/7/18 下午2:41
 */
public abstract class AbstractLifeCycle implements LifeCycle {
    private final AtomicInteger state = new AtomicInteger(Constants.INITIAL);

    protected abstract void doInit();

    protected abstract void doExit() throws InterruptedException;

    @Override
    public void init() {
        if (state.compareAndSet(Constants.INITIAL, Constants.RUNNING)) {
            doInit();
        } else {
            throw new RuntimeException(Constants.UNREACHED);
        }
    }

    @Override
    public void exit() throws InterruptedException {
        if (state.compareAndSet(Constants.RUNNING, Constants.STOPPED)) {
            doExit();
        } else {
            throw new RuntimeException(Constants.UNREACHED);
        }
    }
}
