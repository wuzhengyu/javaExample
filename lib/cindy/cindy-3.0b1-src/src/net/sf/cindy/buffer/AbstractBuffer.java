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
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.sf.cindy.Buffer;
import net.sf.cindy.util.Charset;
import net.sf.cindy.util.Configuration;

/**
 * Abstract buffer.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public abstract class AbstractBuffer implements Buffer {

    protected static final int WRITE_PACKET_SIZE = Configuration
            .getWritePacketSize();

    private boolean readonly;
    private boolean bigEndian = true;
    private boolean permanent;
    private boolean released;

    private int mark = -1;
    private int position = 0;
    private int limit;
    private int capacity;

    private final int offset;

    protected AbstractBuffer(int offset, int capacity) {
        this.offset = offset;
        this.capacity = capacity;

        limit(capacity);
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean b) {
        checkReleased();
        this.permanent = b;
    }

    public boolean isReleased() {
        return released;
    }

    public synchronized void release() {
        if (!(permanent || released)) {
            try {
                _release();
            } finally {
                released = true;
            }
        }
    }

    protected abstract void _release();

    public boolean isReadonly() {
        return readonly;
    }

    protected Buffer setReadonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public Buffer setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
        return this;
    }

    public Buffer skip(int size) {
        if (size != 0)
            position(position + size);
        return this;
    }

    public int capacity() {
        return capacity;
    }

    protected Buffer capacity(int newCapacity) {
        if (newCapacity < 0)
            throw new IllegalArgumentException();
        capacity = newCapacity;
        return limit(Math.min(limit, capacity));
    }

    public int limit() {
        return limit;
    }

    public Buffer limit(int newLimit) {
        if ((newLimit > capacity) || (newLimit < 0))
            throw new IllegalArgumentException();
        limit = newLimit;
        return position(Math.min(position, limit));
    }

    public int position() {
        return position;
    }

    public Buffer position(int newPosition) {
        if ((newPosition > limit) || (newPosition < 0))
            throw new IllegalArgumentException();
        position = newPosition;
        if (mark > position)
            mark = -1;
        return this;
    }

    public Buffer mark() {
        return mark(position);
    }

    protected Buffer mark(int mark) {
        if (mark < 0)
            this.mark = -1;
        else {
            if (mark > position)
                throw new IndexOutOfBoundsException();
            this.mark = mark;
        }
        return this;
    }

    protected int getMark() {
        return mark;
    }

    protected final int getIndex(int i, int len) {
        checkReleased();
        if ((len < 0) || (i < 0) || (len > limit - i))
            throw new IndexOutOfBoundsException();
        return i + offset;
    }

    protected final int getIndex(int len) {
        checkReleased();
        if (limit - position < len)
            throw new BufferUnderflowException();
        int p = position + offset;
        position += len;
        return p;
    }

    protected final int putIndex(int i, int len) {
        checkReleased();
        checkReadonly();
        if ((len < 0) || (i < 0) || (len > limit - i))
            throw new IndexOutOfBoundsException();
        return i + offset;
    }

    protected final int putIndex(int len) {
        checkReleased();
        checkReadonly();
        if (limit - position < len)
            throw new BufferOverflowException();
        int p = position + offset;
        position += len;
        return p;
    }

    protected final static void checkBounds(int off, int len, int size) {
        // all less than 0
        if ((off | len | (off + len) | (size - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
    }

    protected final void checkReadonly() {
        if (isReadonly())
            throw new ReadOnlyBufferException();
    }

    protected final void checkReleased() {
        if (isReleased())
            throw new ReleasedBufferException();
    }

    public Buffer reset() {
        if (mark < 0)
            throw new InvalidMarkException();
        position = mark;
        return this;
    }

    public Buffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }

    public Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    public Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

    public int remaining() {
        return limit - position;
    }

    public boolean hasRemaining() {
        return position < limit;
    }

    public int indexOf(byte[] b) {
        if (b.length == 0)
            return position;

        int startIndex = getIndex(0);
        int endIndex = startIndex + remaining() - b.length;
        byte first = b[0];

        Label: for (int i = startIndex; i <= endIndex; i++) {
            // look for the first character
            if (_get(i) == first) {
                for (int j = 1; j < b.length; j++) {
                    if (_get(i + j) != b[j])
                        continue Label;
                }
                return i - startIndex + position;
            }
        }
        return -1;
    }

    public int write(WritableByteChannel channel) throws IOException {
        int writeCount = 0;
        ByteBuffer buffer = asByteBuffer();
        if (buffer.remaining() > WRITE_PACKET_SIZE)
            buffer.limit(buffer.position() + WRITE_PACKET_SIZE);
        try {
            writeCount = channel.write(buffer);
        } finally {
            skip(writeCount);
        }
        return writeCount;
    }

    public int read(ReadableByteChannel channel) throws IOException {
        int readCount = 0;
        try {
            readCount = channel.read(asByteBuffer());
        } finally {
            if (readCount > 0)
                skip(readCount);
        }
        return readCount;
    }

    public Buffer asReadOnlyBuffer() {
        return ((AbstractBuffer) duplicate()).setReadonly(true);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Buffer))
            return false;
        Buffer buffer = (Buffer) obj;
        if (this.remaining() != buffer.remaining())
            return false;
        int start = getIndex(0);
        int end = start + remaining();
        for (int i = start, j = buffer.position(); i < end; i++, j++) {
            if (_get(i) != buffer.get(j))
                return false;
        }
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=").append(position);
        sb.append(" lim=").append(limit);
        sb.append(" cap=").append(capacity);
        sb.append("]");
        return sb.toString();
    }

    public String dump() {
        ByteBuffer content = (ByteBuffer) asByteBuffer().clear();
        String separator = System.getProperty("line.separator");

        int cols = 16;
        int rows = (int) Math.ceil((double) capacity / cols);
        StringBuffer sb = new StringBuffer(rows * cols * 6);
        byte[] row = new byte[cols];

        sb.append(toString()).append(separator);

        int pos = 0;
        for (int i = 0; i < rows; i++) {
            // dump position
            if (pos != 0)
                sb.append(separator);
            String posHex = Integer.toHexString(pos);
            for (int j = 8 - posHex.length(); j > 0; j--) {
                sb.append(0);
            }
            sb.append(posHex).append("h:");

            // dump hex content
            if (content.remaining() < cols)
                row = new byte[content.remaining()];
            content.get(row);

            for (int j = 0; j < cols; j++) {
                if (pos == position || pos == limit)
                    sb.append("'");
                else
                    sb.append(" ");
                pos++;
                if (j < row.length) {
                    String hex = Integer.toHexString(row[j] & 0xff);
                    if (hex.length() < 2)
                        sb.append(0);
                    sb.append(hex.toUpperCase());
                } else
                    sb.append("  ");
            }
            sb.append(" ; ");

            // dump content
            String s = Charset.SYSTEM.decode(row);
            for (int j = 0; j < s.length(); j++) {
                char c = s.charAt(j);
                sb.append(Character.isISOControl(c) ? '.' : c);
            }
        }
        return sb.toString();
    }

    /**
     * Put byte without check.
     * 
     * @param index
     *            index
     * @param b
     *            byte
     */
    protected abstract void _put(int index, byte b);

    /**
     * Get byte without check.
     * 
     * @param index
     *            index
     * @return byte
     */
    protected abstract byte _get(int index);

    public byte get() {
        return _get(getIndex(1));
    }

    public byte get(int index) {
        return _get(getIndex(index, 1));
    }

    public Buffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    public Buffer get(int index, byte[] dst) {
        return get(index, dst, 0, dst.length);
    }

    public Buffer get(byte[] dst, int offset, int length) {
        return get(ByteBuffer.wrap(dst, offset, length));
    }

    public Buffer get(int index, byte[] dst, int offset, int length) {
        return get(index, ByteBuffer.wrap(dst, offset, length));
    }

    public Buffer get(ByteBuffer dst) {
        return get(dst, dst.remaining());
    }

    public Buffer get(int index, ByteBuffer dst) {
        return get(index, dst, dst.remaining());
    }

    public Buffer get(ByteBuffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        getIndex(length);
        ByteBuffer buffer = asByteBuffer();
        buffer.limit(buffer.position());
        buffer.position(buffer.position() - length);
        dst.put(buffer);
        return this;
    }

    public Buffer get(int index, ByteBuffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        getIndex(index, length);
        ByteBuffer buffer = asByteBuffer();
        buffer.position(index);
        buffer.limit(index + length);
        dst.put(buffer);
        return this;
    }

    public Buffer get(Buffer dst) {
        return get(dst, dst.remaining());
    }

    public Buffer get(int index, Buffer dst) {
        return get(index, dst, dst.remaining());
    }

    public Buffer get(Buffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        getIndex(length);
        ByteBuffer buffer = asByteBuffer();
        buffer.limit(buffer.position());
        buffer.position(buffer.position() - length);
        dst.put(buffer);
        return this;
    }

    public Buffer get(int index, Buffer dst, int length) {
        checkBounds(0, length, dst.remaining());
        getIndex(index, length);
        ByteBuffer buffer = asByteBuffer();
        buffer.position(index);
        buffer.limit(index + length);
        dst.put(buffer);
        return this;
    }

    public Buffer put(byte b) {
        _put(putIndex(1), b);
        return this;
    }

    public Buffer put(int index, byte b) {
        _put(putIndex(index, 1), b);
        return this;
    }

    public Buffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    public Buffer put(int index, byte[] src) {
        return put(index, src, 0, src.length);
    }

    public Buffer put(byte[] src, int offset, int length) {
        return put(ByteBuffer.wrap(src, offset, length));
    }

    public Buffer put(int index, byte[] src, int offset, int length) {
        return put(index, ByteBuffer.wrap(src, offset, length));
    }

    public Buffer put(ByteBuffer src) {
        return put(src, src.remaining());
    }

    public Buffer put(int index, ByteBuffer src) {
        return put(index, src, src.remaining());
    }

    public Buffer put(ByteBuffer src, int length) {
        checkBounds(0, length, src.remaining());
        int pos = putIndex(length);
        for (int i = 0; i < length; i++) {
            _put(pos + i, src.get());
        }
        return this;
    }

    public Buffer put(int index, ByteBuffer src, int length) {
        checkBounds(0, length, src.remaining());
        int pos = putIndex(index, length);
        for (int i = 0; i < length; i++) {
            _put(pos + i, src.get());
        }
        return this;
    }

    public Buffer put(Buffer src) {
        return put(src, src.remaining());
    }

    public Buffer put(int index, Buffer src) {
        return put(index, src, src.remaining());
    }

    public Buffer put(Buffer src, int length) {
        checkBounds(0, length, src.remaining());
        int pos = putIndex(length);
        for (int i = 0; i < length; i++) {
            _put(pos + i, src.get());
        }
        return this;
    }

    public Buffer put(int index, Buffer src, int length) {
        checkBounds(0, length, src.remaining());
        int pos = putIndex(index, length);
        for (int i = 0; i < length; i++) {
            _put(pos + i, src.get());
        }
        return this;
    }

    public char getChar() {
        return (char) getShort();
    }

    public char getChar(int index) {
        return (char) getShort(index);
    }

    public Buffer putChar(char c) {
        return putShort((short) c);
    }

    public Buffer putChar(int index, char c) {
        return putShort(index, (short) c);
    }

    public short getShort() {
        return Bits.decodeShort(this, getIndex(2));
    }

    public short getShort(int index) {
        return Bits.decodeShort(this, getIndex(index, 2));
    }

    public Buffer putShort(short s) {
        return Bits.encodeShort(this, putIndex(2), s);
    }

    public Buffer putShort(int index, short s) {
        return Bits.encodeShort(this, putIndex(index, 2), s);
    }

    public int getInt() {
        return Bits.decodeInt(this, getIndex(4));
    }

    public int getInt(int index) {
        return Bits.decodeInt(this, getIndex(index, 4));
    }

    public Buffer putInt(int i) {
        return Bits.encodeInt(this, putIndex(4), i);
    }

    public Buffer putInt(int index, int i) {
        return Bits.encodeInt(this, putIndex(index, 4), i);
    }

    public long getLong() {
        return Bits.decodeLong(this, getIndex(8));
    }

    public long getLong(int index) {
        return Bits.decodeLong(this, getIndex(index, 8));
    }

    public Buffer putLong(long l) {
        return Bits.encodeLong(this, putIndex(8), l);
    }

    public Buffer putLong(int index, long l) {
        return Bits.encodeLong(this, putIndex(index, 8), l);
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    public Buffer putFloat(float f) {
        return putInt(Float.floatToRawIntBits(f));
    }

    public Buffer putFloat(int index, float f) {
        return putInt(index, Float.floatToRawIntBits(f));
    }

    public double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

    public double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    public Buffer putDouble(double d) {
        return putLong(Double.doubleToRawLongBits(d));
    }

    public Buffer putDouble(int index, double d) {
        return putLong(index, Double.doubleToRawLongBits(d));
    }

    public short getUnsignedByte() {
        return (short) (get() & 0xff);
    }

    public short getUnsignedByte(int index) {
        return (short) (get(index) & 0xff);
    }

    public Buffer putUnsignedByte(short s) {
        if (s < 0 || s > 0xff)
            throw new IllegalArgumentException();
        return put((byte) s);
    }

    public Buffer putUnsignedByte(int index, short s) {
        if (s < 0 || s > 0xff)
            throw new IllegalArgumentException();
        return put(index, (byte) s);
    }

    public int getUnsignedShort() {
        return getShort() & 0xffff;
    }

    public int getUnsignedShort(int index) {
        return getShort(index) & 0xffff;
    }

    public Buffer putUnsignedShort(int i) {
        if (i < 0 || i > 0xffff)
            throw new IllegalArgumentException();
        return putShort((short) i);
    }

    public Buffer putUnsignedShort(int index, int i) {
        if (i < 0 || i > 0xffff)
            throw new IllegalArgumentException();
        return putShort(index, (short) i);
    }

    public long getUnsignedInt() {
        return getInt() & 0xffffffffL;
    }

    public long getUnsignedInt(int index) {
        return getInt(index) & 0xffffffffL;
    }

    public Buffer putUnsignedInt(long l) {
        if (l < 0 || l > 0xffffffff)
            throw new IllegalArgumentException();
        return putInt((int) l);
    }

    public Buffer putUnsignedInt(int index, long l) {
        if (l < 0 || l > 0xffffffff)
            throw new IllegalArgumentException();
        return putInt(index, (int) l);
    }

    public String getString(Charset charset, int bufferLen) {
        getIndex(bufferLen);
        ByteBuffer buffer = asByteBuffer();
        buffer.limit(buffer.position());
        buffer.position(buffer.limit() - bufferLen);
        return charset.decode(buffer);
    }

    public String getString(int index, Charset charset, int bufferLen) {
        getIndex(index, bufferLen);
        ByteBuffer buffer = asByteBuffer();
        buffer.position(index);
        buffer.limit(index + bufferLen);
        return charset.decode(buffer);
    }

    public Buffer putString(String s, Charset charset) {
        return put(charset.encode(s));
    }

    public Buffer putString(int index, String s, Charset charset) {
        return put(index, charset.encode(s));
    }

}
