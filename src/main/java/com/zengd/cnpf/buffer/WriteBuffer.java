package com.zengd.cnpf.buffer;

import com.zengd.cnpf.exception.ExceptionType;
import com.zengd.cnpf.exception.FrameworkException;
import com.zengd.cnpf.utils.Constants;
import com.zengd.cnpf.utils.NativeUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

/**
 * @author zengd
 * @date 2024/7/18 下午1:51
 */
public final class WriteBuffer implements AutoCloseable {
    private static final int DEFAULT_HEAP_BUFFER_SIZE = 32;
    private MemorySegment segment;
    private long size;
    private long writeIndex;
    private final WriteBufferPolicy policy;

    private WriteBuffer(MemorySegment segment, WriteBufferPolicy policy) {
        this.segment = segment;
        this.size = segment.byteSize();
        this.writeIndex = 0;
        this.policy = policy;
    }

    public static WriteBuffer newDefaultWriteBuffer(Arena arena, int size) {
        return new WriteBuffer(MemorySegment.ofArray(new byte[size]), new DefaultWriteBufferPolicy(arena));
    }

    public static WriteBuffer newHeapWriteBuffer(int size) {
        byte[] bytes = new byte[size];
        return new WriteBuffer(MemorySegment.ofArray(bytes), new HeapWriteBufferPolicy(bytes));
    }

    public static WriteBuffer newFixedWriteBuffer(Arena arena, int size) {
        byte[] bytes = new byte[size];
        return new WriteBuffer(MemorySegment.ofArray(bytes), new FixedWriteBufferPolicy(arena));
    }

    public static WriteBuffer newReservedWriteBuffer(MemorySegment memorySegment) {
        return new WriteBuffer(memorySegment, new ReservedWriteBufferPolicy());
    }

    public long size() {
        return size;
    }

    public long writeIndex() {
        return writeIndex;
    }

    public void resize(long nextIndex) {
        if (nextIndex < 0) {
            throw new FrameworkException(ExceptionType.NATIVE, "Index overflow");
        } else if (nextIndex > size) {
            policy.resize(this, nextIndex);
        }
    }

    public void writeByte(byte b) {
        long nextIndex = writeIndex + 1;
        resize(nextIndex);
        NativeUtil.setByte(segment, writeIndex, b);
        writeIndex = nextIndex;
    }

    public void writeBytes(byte[] b, int off, int len) {
        if (len < 0 || off + len > b.length) {
            throw new RuntimeException("Index out of bound");
        }
        long nextIndex = writeIndex + len;
        resize(nextIndex);
        MemorySegment.copy(MemorySegment.ofArray(b), off, segment, writeIndex, len);
        writeIndex = nextIndex;
    }

    public void writeBytes(byte... bytes) {
        long nextIndex = writeIndex + bytes.length;
        resize(nextIndex);
        MemorySegment.copy(MemorySegment.ofArray(bytes), 0, segment, writeIndex, bytes.length);
        writeIndex = nextIndex;
    }

    public void writeShort(short s) {
        long nextIndex = writeIndex + 2;
        resize(nextIndex);
        NativeUtil.setShort(segment, writeIndex, s);
        writeIndex = nextIndex;
    }

    public void writeInt(int i) {
        long nextIndex = writeIndex + 4;
        resize(nextIndex);
        NativeUtil.setInt(segment, writeIndex, i);
        writeIndex = nextIndex;
    }

    public void writeLong(long l) {
        long nextIndex = writeIndex + 8;
        resize(nextIndex);
        NativeUtil.setLong(segment, writeIndex, l);
        writeIndex = nextIndex;
    }

    public void writeCStr(String str) {
        MemorySegment m = MemorySegment.ofArray(str.getBytes(StandardCharsets.UTF_8));
        long len = m.byteSize();
        long nextIndex = writeIndex + len + 1;
        resize(nextIndex);
        MemorySegment.copy(m, 0, segment, writeIndex, len);
        NativeUtil.setByte(segment, writeIndex + len, Constants.NUT);
        writeIndex = nextIndex;
    }

    public void writeSegment(MemorySegment memorySegment) {
        long len = memorySegment.byteSize();
        long nextIndex = writeIndex + len;
        resize(nextIndex);
        MemorySegment.copy(memorySegment, 0, segment, writeIndex, len);
        writeIndex = nextIndex;
    }

    public void setByte(long index, byte value) {
        if (index + 1 > writeIndex) {
            throw new RuntimeException("Index out of bound");
        }
        NativeUtil.setByte(segment, index, value);
    }

    public void setShort(long index, short value) {
        if (index + 2 > writeIndex) {
            throw new RuntimeException("Index out of bound");
        }
        NativeUtil.setShort(segment, index, value);
    }

    public void setInt(long index, int value) {
        if (index + 4 > writeIndex) {
            throw new RuntimeException("Index out of bound");
        }
        NativeUtil.setInt(segment, index, value);
    }

    public void setLong(long index, long value) {
        if (index + 8 > writeIndex) {
            throw new RuntimeException("Index out of bound");
        }
        NativeUtil.setLong(segment, index, value);
    }

    public MemorySegment content() {
        return writeIndex == size ? segment : segment.asSlice(0L, writeIndex);
    }

    public WriteBuffer truncate(long offset) {
        if (offset > writeIndex) {
            throw new FrameworkException(ExceptionType.NATIVE, "Truncate index overflow");
        }
        WriteBuffer w = new WriteBuffer(segment.asSlice(offset, size - offset), policy);
        w.writeIndex = writeIndex - offset;
        return w;
    }


    @Override
    public void close() {
        policy.close(this);
    }

    /**
     * 堆内存扩容策略
     */
    static final class HeapWriteBufferPolicy implements WriteBufferPolicy {
        private byte[] data;

        public HeapWriteBufferPolicy(byte[] data) {
            this.data = data;
        }

        @Override
        public void resize(WriteBuffer writeBuffer, long nextIndex) {
            if (nextIndex > Integer.MAX_VALUE) {
                throw new FrameworkException(ExceptionType.NATIVE, "Heap writeBuffer size overflow");
            }
            int newLen = Math.max((int) nextIndex, data.length << 1);
            if (newLen < 0) {
                throw new FrameworkException(ExceptionType.NATIVE, "MemorySize overflow");
            }
            byte[] newData = new byte[newLen];
            System.arraycopy(data, 0, newData, 0, (int) writeBuffer.writeIndex);
            data = newData;
            writeBuffer.segment = MemorySegment.ofArray(newData);
            writeBuffer.size = newLen;
        }

        @Override
        public void close(WriteBuffer writeBuffer) {
            // No external close operation needed for heapWriteBuffer
        }

    }

    /**
     * 固定大小堆外内存扩容策略
     */
    record FixedWriteBufferPolicy(Arena arena) implements WriteBufferPolicy {

        @Override
        public void resize(WriteBuffer writeBuffer, long nextIndex) {
            throw new FrameworkException(ExceptionType.NATIVE, "Current writeBuffer shouldn't be resized");
        }

        @Override
        public void close(WriteBuffer writeBuffer) {
            arena.close();
        }
    }

    /**
     * 默认扩容策略，所有内存都由同一个arena管理
     */
    record DefaultWriteBufferPolicy(Arena arena) implements WriteBufferPolicy {
        @Override
        public void resize(WriteBuffer writeBuffer, long nextIndex) {
            long newLen = Math.max(nextIndex, writeBuffer.size() << 1);
            if (newLen < 0) {
                throw new FrameworkException(ExceptionType.NATIVE, "MemorySize overflow");
            }
            MemorySegment newSegment = arena.allocateArray(ValueLayout.JAVA_BYTE, newLen);
            MemorySegment.copy(writeBuffer.segment, 0, newSegment, 0, writeBuffer.writeIndex);
            writeBuffer.segment = newSegment;
            writeBuffer.size = newLen;
        }

        @Override
        public void close(WriteBuffer writeBuffer) {
            arena.close();
        }
    }

    /**
     * 预留内存模式的堆外内存扩容策略
     */
    static final class ReservedWriteBufferPolicy implements WriteBufferPolicy {
        private Arena arena = null;

        @Override
        public void resize(WriteBuffer writeBuffer, long nextIndex) {
            long newLen = Math.max(nextIndex, writeBuffer.size() << 1);
            if (newLen < 0) {
                throw new FrameworkException(ExceptionType.NATIVE, "MemorySize overflow");
            }
            // 新建arena对象，只管理扩容部分的内存，预留内存另外管理
            if (arena == null) {
                arena = Arena.ofConfined();
            }
            MemorySegment newSegment = arena.allocateArray(ValueLayout.JAVA_BYTE, newLen);
            MemorySegment.copy(writeBuffer.segment, 0, newSegment, 0, writeBuffer.writeIndex);
            writeBuffer.segment = newSegment;
            writeBuffer.size = newLen;
        }

        @Override
        public void close(WriteBuffer writeBuffer) {
            if (arena != null) {
                arena.close();
            }
        }
    }

}
