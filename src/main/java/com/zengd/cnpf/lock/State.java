package com.zengd.cnpf.lock;

/**
 * @author zengd
 * @date 2024/7/18 下午3:33
 */
public final class State {
    private final Mutex mutex = new Mutex();
    private int state;

    public State() {
        this(0);
    }

    public State(int initialState) {
        this.state = initialState;
    }

    public Mutex withMutex() {
        return mutex.acquire();
    }

    public int get() {
        return state;
    }

    public void set(int state) {
        this.state = state;
    }

    public void register(int mask) {
        this.state |= mask;
    }

    public boolean unregister(int mask) {
        boolean r = (state & mask) > 0;
        state &= ~mask;
        return r;
    }

    public boolean cas(int expectedValue, int newValue) {
        if (state == expectedValue) {
            state = newValue;
            return true;
        } else {
            return false;
        }
    }
}
