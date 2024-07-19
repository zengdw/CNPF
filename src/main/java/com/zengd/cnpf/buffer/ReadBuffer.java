package com.zengd.cnpf.buffer;

import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.NativeUtil;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

import static com.zengd.cnpf.utils.NativeUtil.getByte;

/**
 * @author zengd
 * @date 2024/7/18 上午11:28
 */
public final class ReadBuffer {
    private final MemorySegment segment;
    private final long size;
    private long readIndex;

    public ReadBuffer(MemorySegment segment) {
        this.segment = segment;
        this.size = segment.byteSize();
        this.readIndex = 0L;
    }

    public long size() {
        return size;
    }

    public long readIndex() {
        return readIndex;
    }

    public void setReadIndex(long index) {
        if (index < 0 || index > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "ReadIndex out of bound");
        }
        readIndex = index;
    }

    public byte readByte() {
        long nextIndex = readIndex + 1;
        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }
        byte b = getByte(segment, readIndex);
        readIndex = nextIndex;
        return b;
    }

    public byte[] readBytes(int count) {
        long nextIndex = readIndex + count;
        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }
        byte[] result = segment.asSlice(readIndex, count).toArray(ValueLayout.JAVA_BYTE);
        readIndex = nextIndex;
        return result;
    }

    public short readShort() {
        long nextIndex = readIndex + 2;
        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }
        short s = NativeUtil.getShort(segment, readIndex);
        readIndex = nextIndex;
        return s;
    }

    public int readInt() {
        long nextIndex = readIndex + 4;
        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }
        int i = NativeUtil.getInt(segment, readIndex);
        readIndex = nextIndex;
        return i;
    }

    public long readLong() {
        long nextIndex = readIndex + 8;
        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }
        long l = NativeUtil.getLong(segment, readIndex);
        readIndex = nextIndex;
        return l;
    }

    public MemorySegment readSegment(long count) {
        long nextIndex = readIndex + count;
        if (nextIndex > size) {
            throw new FrameworkException(ExceptionType.NATIVE, "read index overflow");
        }
        MemorySegment result = segment.asSlice(readIndex, count);
        readIndex = nextIndex;
        return result;
    }

    /**
     * 读取到堆内存
     *
     * @param count 读取数据长度
     */
    public MemorySegment readHeapSegment(long count) {
        MemorySegment m = readSegment(count);
        if (m.isNative()) {
            long len = m.byteSize();
            byte[] bytes = new byte[(int) len];
            // 创建一个堆内存
            MemorySegment h = MemorySegment.ofArray(bytes);
            MemorySegment.copy(m, 0, h, 0, len);
            return h;
        } else {
            return m;
        }
    }

    public byte[] readUntil(byte... separators) {
        for (long cur = readIndex; cur <= size - separators.length; cur++) {
            if (NativeUtil.matches(segment, cur, separators)) {
                byte[] result = cur == readIndex ? Constants.EMPTY_BYTES : segment.asSlice(readIndex, cur - readIndex).toArray(ValueLayout.JAVA_BYTE);
                readIndex = cur + separators.length;
                return result;
            }
        }
        return null;
    }

    public String readCStr() {
        byte[] bytes = readUntil(Constants.NUT);
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
