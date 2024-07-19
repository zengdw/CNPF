package com.zengd.cnpf.main;

/**
 * @author zengd
 * @date 2024/7/18 下午2:40
 */
public interface LifeCycle {
    void init();

    void exit() throws InterruptedException;
}
