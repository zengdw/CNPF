package com.zengd.cnpf.lock;

import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zengd
 */
public record Mutex(Lock lock) implements AutoCloseable {
    public Mutex {
        if (lock == null) {
            throw new FrameworkException(ExceptionType.CONTEXT, Constants.UNREACHED);
        }
    }

    public Mutex() {
        this(new ReentrantLock());
    }

    public Mutex acquire() {
        lock.lock();
        return this;
    }

    @Override
    public void close() {
        lock.unlock();
    }
}

