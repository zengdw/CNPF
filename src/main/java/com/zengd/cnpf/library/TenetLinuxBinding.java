package com.zengd.cnpf.library;

import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.NativeUtil;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * @author zengd
 * @date 2024/7/22 下午3:15
 */
public final class TenetLinuxBinding {
    private static final MethodHandle ERRNO_METHOD_HANDLE;

    static {
        SymbolLookup symbolLookup = NativeUtil.loadLibrary("tenet");
        ERRNO_METHOD_HANDLE = NativeUtil.methodHandle(symbolLookup, "errno", FunctionDescriptor.of(ValueLayout.JAVA_INT));
    }

    public static int errno() {
        try {
            return (int) ERRNO_METHOD_HANDLE.invokeExact();
        } catch (Throwable throwable) {
            throw new RuntimeException(Constants.UNREACHED, throwable);
        }
    }
}
