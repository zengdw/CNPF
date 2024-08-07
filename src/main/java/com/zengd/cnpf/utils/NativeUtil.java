package com.zengd.cnpf.utils;

import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.vo.OsType;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.zengd.cnpf.vo.OsType.*;

/**
 * 堆外内存操作方法类
 *
 * @author zengd
 * @date 2024/7/11 下午4:02
 */
@SuppressWarnings("preview")
public class NativeUtil {
    public static final MemorySegment NULL_POINTER = MemorySegment.ofAddress(0L);
    private static final VarHandle BYTE_HANDLE = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_BYTE);
    private static final VarHandle SHORT_HANDLE = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_SHORT_UNALIGNED);
    private static final VarHandle INT_HANDLE = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_INT_UNALIGNED);
    private static final VarHandle LONG_HANDLE = MethodHandles.memorySegmentViewVarHandle(ValueLayout.JAVA_LONG_UNALIGNED);
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final OsType OS_TYPE = detectOsType();

    private static final Arena globalArena = Arena.global();
    private static final Linker linker = Linker.nativeLinker();
    private static final String libPath = System.getProperty("TENET_LIBRARY_PATH");
    private static final ConcurrentHashMap<String, SymbolLookup> libraryCache = new ConcurrentHashMap<>();

    private NativeUtil() {
        throw new UnsupportedOperationException();
    }

    private static String getDynamicLibraryName(String identifier) {
        return switch (OS_TYPE) {
            case Windows -> STR."lib\{identifier}.dll";
            case Linux -> STR."lib\{identifier}.so";
            case MacOS -> STR."lib\{identifier}.dylib";
            default -> throw new FrameworkException(ExceptionType.NATIVE, "Unrecognized operating system");
        };
    }

    public static SymbolLookup loadLibrary(String identifier) {
        if (libPath == null) {
            throw new FrameworkException(ExceptionType.NATIVE, "Global libPath not found");
        }
        return libraryCache.computeIfAbsent(identifier, i -> SymbolLookup.libraryLookup(STR."\{libPath}/\{getDynamicLibraryName(i)}", Arena.global()));
    }


    public static MethodHandle methodHandle(SymbolLookup lookup, String methodName, FunctionDescriptor functionDescriptor, Linker.Option... options) {
        MemorySegment methodPointer = lookup.find(methodName)
                .orElseThrow(() -> new FrameworkException(ExceptionType.NATIVE, STR."Unable to load target native method : \{methodName}"));
        return linker.downcallHandle(methodPointer, functionDescriptor, options);
    }

    public static MethodHandle methodHandle(SymbolLookup lookup, List<String> methodNames, FunctionDescriptor functionDescriptor, Linker.Option... options) {
        for (String methodName : methodNames) {
            Optional<MemorySegment> methodPointer = lookup.find(methodName);
            if (methodPointer.isPresent()) {
                return linker.downcallHandle(methodPointer.get(), functionDescriptor, options);
            }
        }
        throw new FrameworkException(ExceptionType.NATIVE, STR."Unable to load target native method : \{methodNames}");
    }

    private static OsType detectOsType() {
        if (OS_NAME.contains("windows")) {
            return Windows;
        } else if (OS_NAME.contains("linux")) {
            return Linux;
        } else if (OS_NAME.contains("mac") && OS_NAME.contains("os")) {
            return MacOS;
        } else {
            return OsType.Unknown;
        }
    }

    public static OsType ostype() {
        return OS_TYPE;
    }

    public static int castInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
        }
        return (int) l;
    }

    public static int getCpuCores() {
        return CPU_CORES;
    }

    public static boolean checkNullPointer(MemorySegment memorySegment) {
        return memorySegment == null || memorySegment.address() == 0L;
    }

    public static byte getByte(MemorySegment memorySegment, long index) {
        return (byte) BYTE_HANDLE.get(memorySegment, index);
    }

    public static void setByte(MemorySegment memorySegment, long index, byte value) {
        BYTE_HANDLE.set(memorySegment, index, value);
    }

    public static short getShort(MemorySegment memorySegment, long index) {
        return (short) SHORT_HANDLE.get(memorySegment, index);
    }

    public static void setShort(MemorySegment memorySegment, long index, short value) {
        SHORT_HANDLE.set(memorySegment, index, value);
    }

    public static int getInt(MemorySegment memorySegment, long index) {
        return (int) INT_HANDLE.get(memorySegment, index);
    }

    public static void setInt(MemorySegment memorySegment, long index, int value) {
        INT_HANDLE.set(memorySegment, index, value);
    }

    public static long getLong(MemorySegment memorySegment, long index) {
        return (long) LONG_HANDLE.get(memorySegment, index);
    }

    public static void setLong(MemorySegment memorySegment, long index, long value) {
        LONG_HANDLE.set(memorySegment, index, value);
    }

    public static String getStr(MemorySegment memorySegment) {
        return getStr(memorySegment, 0);
    }

    public static String getStr(MemorySegment ptr, int maxLength) {
        if (maxLength > 0) {
            byte[] bytes = new byte[maxLength];
            for (int i = 0; i < maxLength; i++) {
                byte b = getByte(ptr, i);
                if (b == Constants.NUT) {
                    return new String(bytes, 0, i, StandardCharsets.UTF_8);
                } else {
                    bytes[i] = b;
                }
            }
        } else {
            return ptr.getUtf8String(0);
        }
        throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
    }

    public static MemorySegment allocateStr(Arena arena, String str) {
        return arena.allocateUtf8String(str);
    }

    public static MemorySegment allocateStr(Arena arena, String str, int len) {
        MemorySegment strSegment = MemorySegment.ofArray(str.getBytes(StandardCharsets.UTF_8));
        long size = strSegment.byteSize();
        if (len < size + 1) {
            throw new RuntimeException("String out of range");
        }
        MemorySegment memorySegment = arena.allocateArray(ValueLayout.JAVA_BYTE, len);
        MemorySegment.copy(strSegment, 0, memorySegment, 0, size);
        setByte(memorySegment, size, Constants.NUT);
        return memorySegment;
    }

    public static boolean matches(MemorySegment m, long offset, byte[] bytes) {
        for (int index = 0; index < bytes.length; index++) {
            if (getByte(m, offset + index) != bytes[index]) {
                return false;
            }
        }
        return true;
    }
}
