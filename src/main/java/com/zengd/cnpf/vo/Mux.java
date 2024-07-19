package com.zengd.cnpf.vo;


import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.NativeUtil;

import java.lang.foreign.MemorySegment;

/**
 * @author zengd
 */
public record Mux(
        MemorySegment winHandle,
        int epfd,
        int kqfd
) {
    public static Mux win(MemorySegment winHandle) {
        return new Mux(winHandle, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public static Mux linux(int epfd) {
        return new Mux(NativeUtil.NULL_POINTER, epfd, Integer.MIN_VALUE);
    }

    public static Mux mac(int kqfd) {
        return new Mux(NativeUtil.NULL_POINTER, Integer.MIN_VALUE, kqfd);
    }

    @Override
    public String toString() {
        if (winHandle != NativeUtil.NULL_POINTER) {
            return String.valueOf(winHandle.address());
        } else if (epfd != Integer.MIN_VALUE) {
            return String.valueOf(epfd);
        } else if (kqfd != Integer.MIN_VALUE) {
            return String.valueOf(kqfd);
        } else {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

}


