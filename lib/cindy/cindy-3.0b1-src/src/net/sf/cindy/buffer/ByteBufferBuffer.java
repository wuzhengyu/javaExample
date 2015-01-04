/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cindy.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.sf.cindy.Buffer;

/**
 * Implementation of <code>Buffer</code> which use
 * <code>java.nio.ByteBuffer</code> as container.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ByteBufferBuffer extends AbstractBuffer {

    public static ByteBufferBuffer allocate(int capacity, boolean direct) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new ByteBufferBuffer(direct ? ByteBuffer
                .allocateDirect(capacity) : ByteBuffer.allocate(capacity), 0,
                capacity);
    }

    public static ByteBufferBuffer wrap(ByteBuffer buffer) {
        ByteBufferBuffer result = new ByteBufferBuffer(buffer.duplicate(), 0,
                buffer.capacity());
        result.limit(buffer.limit()).position(buffer.position());
        result.setReadonly(buffer.isReadOnly());
        result.setBigEndian(buffer.order() == ByteOrder.BIG_ENDIAN);
        return result;
    }

    private static final ByteBuffer EMPTY_DIRECT_CONTENT = ByteBuffer
            .allocateDirect(0);
    private static final ByteBuffer EMPTY_HEAP_CONTENT = ByteBuffer.allocate(0);

    private ByteBuffer content;

    protected ByteBufferBuffer(ByteBuffer content, int offset, int capacity) {
        super(offset, capacity);
        this.content = content;
    }

    private class DelegateReleaseBuffer extends ByteBufferBuffer {

        public DelegateReleaseBuffer(int offset, int capacity) {
            super(content, offset, capacity);
        }

        public boolean isReleased() {
            return ByteBufferBuffer.this.isReleased();
        }

        public void release() {
            ByteBufferBuffer.this.release();
        }

        public boolean isPermanent() {
            return ByteBufferBuffer.this.isPermanent();
        }

        public void setPermanent(boolean b) {
            ByteBufferBuffer.this.setPermanent(b);
        }

    }

    public boolean isDirect() {
        return content.isDirect();
    }

    public ByteBuffer asByteBuffer() {
        int limit = content.limit();
        content.position(getIndex(0, 0));
        content.limit(content.position() + capacity());
        ByteBuffer buffer = content.slice();
        buffer.position(position());
        buffer.limit(limit());
        content.limit(limit);
        return isReadonly() ? buffer.asReadOnlyBuffer() : buffer;
    }

    public Buffer duplicate() {
        ByteBufferBuffer buffer = new DelegateReleaseBuffer(getIndex(0, 0),
                capacity());
        buffer.limit(limit()).position(position());
        buffer.mark(getMark());
        buffer.setReadonly(isReadonly());
        return buffer;
    }

    public Buffer slice() {
        return new DelegateReleaseBuffer(getIndex(0), remaining())
                .setReadonly(isReadonly());
    }

    public Buffer compact() {
        checkReadonly();
        ByteBuffer duplicate = content.duplicate();
        duplicate.position(getIndex(0));
        duplicate.limit(duplicate.position() + remaining());
        content.position(getIndex(0, 0));
        content.put(duplicate);
        position(remaining()).limit(capacity());
        return this;
    }

    protected void _release() {
        content = isDirect() ? EMPTY_DIRECT_CONTENT : EMPTY_HEAP_CONTENT;
    }

    protected byte _get(int index) {
        return content.get(index);
    }

    protected void _put(int index, byte b) {
        content.put(index, b);
    }

    public int write(WritableByteChannel channel) throws IOException {
        int writeCount = 0;
        int limit = content.limit();
        int position = getIndex(0);
        content.position(position).limit(
                position + Math.min(WRITE_PACKET_SIZE, remaining()));
        try {
            writeCount = channel.write(content);
        } finally {
            content.limit(limit);
            skip(writeCount);
        }
        return writeCount;
    }

    public int read(ReadableByteChannel channel) throws IOException {
        checkReadonly();
        int readCount = 0;
        int limit = content.limit();
        try {
            int position = getIndex(0);
            content.position(position).limit(position + remaining());
            readCount = channel.read(content);
        } finally {
            content.limit(limit);
            if (readCount > 0)
                skip(readCount);
        }
        return readCount;
    }

    public Buffer get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        content.position(getIndex(length));
        content.get(dst, offset, length);
        return this;
    }

    public Buffer get(int index, byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        content.position(getIndex(index, length));
        content.get(dst, offset, length);
        return this;
    }

    public Buffer get(ByteBuffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        content.position(getIndex(length));
        int limit = content.limit();
        content.limit(content.position() + length);
        dst.put(content);
        content.limit(limit);
        return this;
    }

    public Buffer get(int index, ByteBuffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        content.position(getIndex(index, length));
        int limit = content.limit();
        content.limit(content.position() + length);
        dst.put(content);
        content.limit(limit);
        return this;
    }

    public Buffer get(Buffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        content.position(getIndex(length));
        dst.put(content, length);
        return this;
    }

    public Buffer get(int index, Buffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        content.position(getIndex(index, length));
        dst.put(content, length);
        return this;
    }

    public Buffer put(byte[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        content.position(putIndex(length));
        content.put(src, offset, length);
        return this;
    }

    public Buffer put(int index, byte[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        content.position(putIndex(index, length));
        content.put(src, offset, length);
        return this;
    }

    public Buffer put(ByteBuffer src, int length) {
        checkBounds(0, length, src.remaining());
        content.position(putIndex(length));
        int limit = src.limit();
        src.limit(src.position() + length);
        content.put(src);
        src.limit(limit);
        return this;
    }

    public Buffer put(int index, ByteBuffer src, int length) {
        checkBounds(0, length, src.remaining());
        content.position(putIndex(index, length));
        int limit = src.limit();
        src.limit(src.position() + length);
        content.put(src);
        src.limit(limit);
        return this;
    }

    public Buffer put(Buffer src, int length) {
        checkBounds(0, length, src.remaining());
        content.position(putIndex(length));
        src.get(content, length);
        return this;
    }

    public Buffer put(int index, Buffer src, int length) {
        checkBounds(0, length, src.remaining());
        content.position(putIndex(index, length));
        src.get(content, length);
        return this;
    }

    public char getChar() {
        return content.getChar(getIndex(2));
    }

    public char getChar(int index) {
        return content.getChar(getIndex(index, 2));
    }

    public Buffer putChar(char c) {
        content.putChar(putIndex(2), c);
        return this;
    }

    public Buffer putChar(int index, char c) {
        content.putChar(putIndex(index, 2), c);
        return this;
    }

    public short getShort() {
        return content.getShort(getIndex(2));
    }

    public short getShort(int index) {
        return content.getShort(getIndex(index, 2));
    }

    public Buffer putShort(short s) {
        content.putShort(putIndex(2), s);
        return this;
    }

    public Buffer putShort(int index, short s) {
        content.putShort(putIndex(index, 2), s);
        return this;
    }

    public int getInt() {
        return content.getInt(getIndex(4));
    }

    public int getInt(int index) {
        return content.getInt(getIndex(index, 4));
    }

    public Buffer putInt(int i) {
        content.putInt(putIndex(4), i);
        return this;
    }

    public Buffer putInt(int index, int i) {
        content.putInt(putIndex(index, 4), i);
        return this;
    }

    public long getLong() {
        return content.getLong(getIndex(8));
    }

    public long getLong(int index) {
        return content.getLong(getIndex(index, 8));
    }

    public Buffer putLong(long l) {
        content.putLong(putIndex(8), l);
        return this;
    }

    public Buffer putLong(int index, long l) {
        content.putLong(putIndex(index, 8), l);
        return this;
    }

    public float getFloat() {
        return content.getFloat(getIndex(4));
    }

    public float getFloat(int index) {
        return content.getFloat(getIndex(index, 4));
    }

    public Buffer putFloat(float f) {
        content.putFloat(putIndex(4), f);
        return this;
    }

    public Buffer putFloat(int index, float f) {
        content.putFloat(putIndex(index, 4), f);
        return this;
    }

    public double getDouble() {
        return content.getDouble(getIndex(8));
    }

    public double getDouble(int index) {
        return content.getDouble(getIndex(index, 8));
    }

    public Buffer putDouble(double d) {
        content.putDouble(putIndex(8), d);
        return this;
    }

    public Buffer putDouble(int index, double d) {
        content.putDouble(putIndex(index, 8), d);
        return this;
    }
}
