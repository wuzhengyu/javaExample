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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.sf.cindy.Buffer;

/**
 * Implementation of <code>Buffer</code> which use byte array as container.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class ByteArrayBuffer extends AbstractBuffer {

    public static ByteArrayBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new ByteArrayBuffer(new byte[capacity], 0, capacity);
    }

    public static ByteArrayBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public static ByteArrayBuffer wrap(byte[] array, int offset, int length) {
        checkBounds(offset, length, array.length);
        ByteArrayBuffer buffer = new ByteArrayBuffer(array, 0, array.length);
        buffer.limit(offset + length);
        buffer.position(offset);
        return buffer;
    }

    private static final byte[] EMPTY_CONTENT = new byte[0];

    private byte[] content;

    protected ByteArrayBuffer(byte[] content, int offset, int capacity) {
        super(offset, capacity);
        this.content = content;
    }

    private class DelegateReleaseBuffer extends ByteArrayBuffer {

        public DelegateReleaseBuffer(int offset, int capacity) {
            super(content, offset, capacity);
        }

        public boolean isReleased() {
            return ByteArrayBuffer.this.isReleased();
        }

        public void release() {
            ByteArrayBuffer.this.release();
        }

        public boolean isPermanent() {
            return ByteArrayBuffer.this.isPermanent();
        }

        public void setPermanent(boolean b) {
            ByteArrayBuffer.this.setPermanent(b);
        }

    }

    public boolean isDirect() {
        return false;
    }

    public ByteBuffer asByteBuffer() {
        ByteBuffer buffer = ByteBuffer
                .wrap(content, getIndex(0, 0), capacity()).slice();
        buffer.position(position()).limit(limit());
        return isReadonly() ? buffer.asReadOnlyBuffer() : buffer;
    }

    public Buffer duplicate() {
        ByteArrayBuffer buffer = new DelegateReleaseBuffer(getIndex(0, 0),
                capacity());
        buffer.limit(limit()).position(position());
        buffer.mark(getMark());
        buffer.setReadonly(isReadonly());
        return buffer;
    }

    public Buffer slice() {
        return new DelegateReleaseBuffer(getIndex(0), remaining());
    }

    public Buffer compact() {
        checkReadonly();
        System.arraycopy(content, getIndex(0), content, getIndex(0, 0),
                remaining());
        position(remaining()).limit(capacity());
        return this;
    }

    protected void _release() {
        content = EMPTY_CONTENT;
    }

    protected byte _get(int index) {
        return content[index];
    }

    protected void _put(int index, byte b) {
        content[index] = b;
    }

    public int write(WritableByteChannel channel) throws IOException {
        int writeCount = 0;
        try {
            writeCount = channel.write(ByteBuffer.wrap(content, getIndex(0),
                    Math.min(WRITE_PACKET_SIZE, remaining())));
        } finally {
            skip(writeCount);
        }
        return writeCount;
    }

    public int read(ReadableByteChannel channel) throws IOException {
        checkReadonly();
        int readCount = 0;
        try {
            readCount = channel.read(ByteBuffer.wrap(content, getIndex(0),
                    remaining()));
        } finally {
            if (readCount > 0)
                skip(readCount);
        }
        return readCount;
    }

    public Buffer get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        System.arraycopy(content, getIndex(length), dst, offset, length);
        return this;
    }

    public Buffer get(int index, byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        System.arraycopy(content, getIndex(index, length), dst, offset, length);
        return this;
    }

    public Buffer get(ByteBuffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        dst.put(content, getIndex(length), length);
        return this;
    }

    public Buffer get(int index, ByteBuffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        dst.put(content, getIndex(index, length), length);
        return this;
    }

    public Buffer get(Buffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        dst.put(content, getIndex(length), length);
        return this;
    }

    public Buffer get(int index, Buffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        dst.put(content, getIndex(index, length), length);
        return this;
    }

    public Buffer put(byte[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        System.arraycopy(src, offset, content, putIndex(length), length);
        return this;
    }

    public Buffer put(int index, byte[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        System.arraycopy(src, offset, content, putIndex(index, length), length);
        return this;
    }

    public Buffer put(ByteBuffer src, int length) {
        checkBounds(0, length, src.remaining());
        src.get(content, putIndex(length), length);
        return this;
    }

    public Buffer put(int index, ByteBuffer src, int length) {
        checkBounds(0, length, src.remaining());
        src.get(content, putIndex(index, length), length);
        return this;
    }

    public Buffer put(Buffer src, int length) {
        checkBounds(0, length, src.remaining());
        src.get(content, putIndex(length), length);
        return this;
    }

    public Buffer put(int index, Buffer src, int length) {
        checkBounds(0, length, src.remaining());
        src.get(content, putIndex(index, length), length);
        return this;
    }

}
