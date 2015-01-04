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
import net.sf.cindy.util.Charset;

/**
 * Delegate buffer.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DelegateBuffer implements Buffer {

    /**
     * Delegate buffer.
     */
    protected final Buffer delegate;

    public DelegateBuffer(Buffer delegate) {
        this.delegate = delegate;
    }

    public Buffer getDelegate() {
        return delegate;
    }

    public ByteBuffer asByteBuffer() {
        return delegate.asByteBuffer();
    }

    public Buffer asReadOnlyBuffer() {
        return delegate.asReadOnlyBuffer();
    }

    public int capacity() {
        return delegate.capacity();
    }

    public Buffer clear() {
        return delegate.clear();
    }

    public Buffer compact() {
        return delegate.compact();
    }

    public String dump() {
        return delegate.dump();
    }

    public Buffer duplicate() {
        return delegate.duplicate();
    }

    public Buffer flip() {
        return delegate.flip();
    }

    public byte get() {
        return delegate.get();
    }

    public Buffer get(Buffer dst, int length) {
        return delegate.get(dst, length);
    }

    public Buffer get(Buffer dst) {
        return delegate.get(dst);
    }

    public Buffer get(byte[] dst, int offset, int length) {
        return delegate.get(dst, offset, length);
    }

    public Buffer get(byte[] dst) {
        return delegate.get(dst);
    }

    public Buffer get(ByteBuffer dst, int length) {
        return delegate.get(dst, length);
    }

    public Buffer get(ByteBuffer dst) {
        return delegate.get(dst);
    }

    public Buffer get(int index, Buffer dst, int length) {
        return delegate.get(index, dst, length);
    }

    public Buffer get(int index, Buffer dst) {
        return delegate.get(index, dst);
    }

    public Buffer get(int index, byte[] dst, int offset, int length) {
        return delegate.get(index, dst, offset, length);
    }

    public Buffer get(int index, byte[] dst) {
        return delegate.get(index, dst);
    }

    public Buffer get(int index, ByteBuffer dst, int length) {
        return delegate.get(index, dst, length);
    }

    public Buffer get(int index, ByteBuffer dst) {
        return delegate.get(index, dst);
    }

    public byte get(int index) {
        return delegate.get(index);
    }

    public char getChar() {
        return delegate.getChar();
    }

    public char getChar(int index) {
        return delegate.getChar(index);
    }

    public double getDouble() {
        return delegate.getDouble();
    }

    public double getDouble(int index) {
        return delegate.getDouble(index);
    }

    public float getFloat() {
        return delegate.getFloat();
    }

    public float getFloat(int index) {
        return delegate.getFloat(index);
    }

    public int getInt() {
        return delegate.getInt();
    }

    public int getInt(int index) {
        return delegate.getInt(index);
    }

    public long getLong() {
        return delegate.getLong();
    }

    public long getLong(int index) {
        return delegate.getLong(index);
    }

    public short getShort() {
        return delegate.getShort();
    }

    public short getShort(int index) {
        return delegate.getShort(index);
    }

    public String getString(Charset charset, int bufferLen) {
        return delegate.getString(charset, bufferLen);
    }

    public String getString(int index, Charset charset, int bufferLen) {
        return delegate.getString(index, charset, bufferLen);
    }

    public short getUnsignedByte() {
        return delegate.getUnsignedByte();
    }

    public short getUnsignedByte(int index) {
        return delegate.getUnsignedByte(index);
    }

    public long getUnsignedInt() {
        return delegate.getUnsignedInt();
    }

    public long getUnsignedInt(int index) {
        return delegate.getUnsignedInt(index);
    }

    public int getUnsignedShort() {
        return delegate.getUnsignedShort();
    }

    public int getUnsignedShort(int index) {
        return delegate.getUnsignedShort(index);
    }

    public boolean hasRemaining() {
        return delegate.hasRemaining();
    }

    public int indexOf(byte[] b) {
        return delegate.indexOf(b);
    }

    public boolean isBigEndian() {
        return delegate.isBigEndian();
    }

    public boolean isDirect() {
        return delegate.isDirect();
    }

    public boolean isPermanent() {
        return delegate.isPermanent();
    }

    public boolean isReadonly() {
        return delegate.isReadonly();
    }

    public boolean isReleased() {
        return delegate.isReleased();
    }

    public int limit() {
        return delegate.limit();
    }

    public Buffer limit(int limit) {
        return delegate.limit(limit);
    }

    public Buffer mark() {
        return delegate.mark();
    }

    public int position() {
        return delegate.position();
    }

    public Buffer position(int position) {
        return delegate.position(position);
    }

    public Buffer put(Buffer src, int length) {
        return delegate.put(src, length);
    }

    public Buffer put(Buffer src) {
        return delegate.put(src);
    }

    public Buffer put(byte b) {
        return delegate.put(b);
    }

    public Buffer put(byte[] src, int offset, int length) {
        return delegate.put(src, offset, length);
    }

    public Buffer put(byte[] src) {
        return delegate.put(src);
    }

    public Buffer put(ByteBuffer src, int length) {
        return delegate.put(src, length);
    }

    public Buffer put(ByteBuffer src) {
        return delegate.put(src);
    }

    public Buffer put(int index, Buffer src, int length) {
        return delegate.put(index, src, length);
    }

    public Buffer put(int index, Buffer src) {
        return delegate.put(index, src);
    }

    public Buffer put(int index, byte b) {
        return delegate.put(index, b);
    }

    public Buffer put(int index, byte[] src, int offset, int length) {
        return delegate.put(index, src, offset, length);
    }

    public Buffer put(int index, byte[] src) {
        return delegate.put(index, src);
    }

    public Buffer put(int index, ByteBuffer src, int length) {
        return delegate.put(index, src, length);
    }

    public Buffer put(int index, ByteBuffer src) {
        return delegate.put(index, src);
    }

    public Buffer putChar(char c) {
        return delegate.putChar(c);
    }

    public Buffer putChar(int index, char c) {
        return delegate.putChar(index, c);
    }

    public Buffer putDouble(double d) {
        return delegate.putDouble(d);
    }

    public Buffer putDouble(int index, double d) {
        return delegate.putDouble(index, d);
    }

    public Buffer putFloat(float f) {
        return delegate.putFloat(f);
    }

    public Buffer putFloat(int index, float f) {
        return delegate.putFloat(index, f);
    }

    public Buffer putInt(int index, int i) {
        return delegate.putInt(index, i);
    }

    public Buffer putInt(int i) {
        return delegate.putInt(i);
    }

    public Buffer putLong(int index, long l) {
        return delegate.putLong(index, l);
    }

    public Buffer putLong(long l) {
        return delegate.putLong(l);
    }

    public Buffer putShort(int index, short s) {
        return delegate.putShort(index, s);
    }

    public Buffer putShort(short s) {
        return delegate.putShort(s);
    }

    public Buffer putString(int index, String s, Charset charset) {
        return delegate.putString(index, s, charset);
    }

    public Buffer putString(String s, Charset charset) {
        return delegate.putString(s, charset);
    }

    public Buffer putUnsignedByte(int index, short s) {
        return delegate.putUnsignedByte(index, s);
    }

    public Buffer putUnsignedByte(short s) {
        return delegate.putUnsignedByte(s);
    }

    public Buffer putUnsignedInt(int index, long l) {
        return delegate.putUnsignedInt(index, l);
    }

    public Buffer putUnsignedInt(long l) {
        return delegate.putUnsignedInt(l);
    }

    public Buffer putUnsignedShort(int index, int i) {
        return delegate.putUnsignedShort(index, i);
    }

    public Buffer putUnsignedShort(int i) {
        return delegate.putUnsignedShort(i);
    }

    public int read(ReadableByteChannel channel) throws IOException {
        return delegate.read(channel);
    }

    public void release() {
        delegate.release();
    }

    public int remaining() {
        return delegate.remaining();
    }

    public Buffer reset() {
        return delegate.reset();
    }

    public Buffer rewind() {
        return delegate.rewind();
    }

    public Buffer setBigEndian(boolean b) {
        return delegate.setBigEndian(b);
    }

    public void setPermanent(boolean b) {
        delegate.setPermanent(b);
    }

    public Buffer skip(int size) {
        return delegate.skip(size);
    }

    public Buffer slice() {
        return delegate.slice();
    }

    public int write(WritableByteChannel channel) throws IOException {
        return delegate.write(channel);
    }

}
