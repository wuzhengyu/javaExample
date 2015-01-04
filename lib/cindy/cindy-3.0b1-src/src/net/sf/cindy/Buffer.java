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
package net.sf.cindy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import net.sf.cindy.util.Charset;

/**
 * A replacement for <code>java.nio.ByteBuffer</code>.
 * <p>
 * Advantage:
 * <ul>
 * <li>cachable</li>
 * <li>some utility method, such as indexOf/putString/getString/getUnsignedXXX</li>
 * <li>can add custom implementation</li>
 * </ul>
 * <p>
 * Buffers are not safe for use by multiple concurrent threads. If a buffer is
 * to be used by more than one thread then access to the buffer should be
 * controlled by appropriate synchronization.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public interface Buffer {

    /**
     * Creates a view of this buffer as a byte buffer. The returned byte
     * buffer's position/limit/capacity equals current position/limit/capacity.
     * The returned <code>ByteBuffer</code> MAY share the content of the
     * <code>Buffer</code>, depends on implementation.
     * 
     * @return the byte buffer
     */
    ByteBuffer asByteBuffer();

    /**
     * Return this buffer is permanent. Permanent buffer can't be released.
     * 
     * @return is permanent
     */
    boolean isPermanent();

    /**
     * Set current buffer is permanent. Permanent buffer can't be released.
     * 
     * @param b
     *            permanent
     */
    void setPermanent(boolean b);

    /**
     * Release this buffer's content if not permanent.
     */
    void release();

    /**
     * Current buffer's content have been released.
     * 
     * @return released
     */
    boolean isReleased();

    /**
     * Writes a sequence of bytes to the channel.
     * 
     * @param channel
     *            channel
     * @return the number of bytes written, possibly zero
     * @throws IOException
     *             any io exception
     */
    int write(WritableByteChannel channel) throws IOException;

    /**
     * Reads a sequence of bytes from the channel.
     * 
     * @param channel
     *            channel
     * @return the number of bytes read, possibly zero, or -1 if the channel has
     *         reached end-of-stream
     * @throws IOException
     *             any io exception
     */
    int read(ReadableByteChannel channel) throws IOException;

    /**
     * This method is a shorthand for: position(position() + size).
     * 
     * @param size
     *            skip size
     * @return the byte buffer
     */
    Buffer skip(int size);

    /**
     * Dump current buffer to string. Help debug.
     * 
     * @return string
     */
    String dump();

    int capacity();

    int position();

    Buffer position(int position);

    int limit();

    Buffer limit(int limit);

    Buffer mark();

    Buffer reset();

    Buffer clear();

    Buffer flip();

    Buffer rewind();

    int remaining();

    boolean hasRemaining();

    boolean isReadonly();

    boolean isBigEndian();

    Buffer setBigEndian(boolean b);

    boolean isDirect();

    Buffer compact();

    Buffer slice();

    Buffer duplicate();

    Buffer asReadOnlyBuffer();

    byte get();

    byte get(int index);

    Buffer get(byte[] dst);

    Buffer get(int index, byte[] dst);

    Buffer get(byte[] dst, int offset, int length);

    Buffer get(int index, byte[] dst, int offset, int length);

    Buffer get(ByteBuffer dst);

    Buffer get(ByteBuffer dst, int length);

    Buffer get(int index, ByteBuffer dst);

    Buffer get(int index, ByteBuffer dst, int length);

    Buffer get(Buffer dst);

    Buffer get(Buffer dst, int length);

    Buffer get(int index, Buffer dst);

    Buffer get(int index, Buffer dst, int length);

    Buffer put(byte b);

    Buffer put(int index, byte b);

    Buffer put(ByteBuffer src);

    Buffer put(ByteBuffer src, int length);

    Buffer put(int index, ByteBuffer src);

    Buffer put(int index, ByteBuffer src, int length);

    Buffer put(Buffer src);

    Buffer put(Buffer src, int length);

    Buffer put(int index, Buffer src);

    Buffer put(int index, Buffer src, int length);

    Buffer put(byte[] src);

    Buffer put(byte[] src, int offset, int length);

    Buffer put(int index, byte[] src);

    Buffer put(int index, byte[] src, int offset, int length);

    char getChar();

    Buffer putChar(char c);

    char getChar(int index);

    Buffer putChar(int index, char c);

    short getShort();

    Buffer putShort(short s);

    short getShort(int index);

    Buffer putShort(int index, short s);

    int getInt();

    Buffer putInt(int i);

    int getInt(int index);

    Buffer putInt(int index, int i);

    long getLong();

    Buffer putLong(long l);

    long getLong(int index);

    Buffer putLong(int index, long l);

    float getFloat();

    Buffer putFloat(float f);

    float getFloat(int index);

    Buffer putFloat(int index, float f);

    double getDouble();

    Buffer putDouble(double d);

    double getDouble(int index);

    Buffer putDouble(int index, double d);

    short getUnsignedByte();

    short getUnsignedByte(int index);

    Buffer putUnsignedByte(short s);

    Buffer putUnsignedByte(int index, short s);

    int getUnsignedShort();

    int getUnsignedShort(int index);

    Buffer putUnsignedShort(int i);

    Buffer putUnsignedShort(int index, int i);

    long getUnsignedInt();

    long getUnsignedInt(int index);

    Buffer putUnsignedInt(long l);

    Buffer putUnsignedInt(int index, long l);

    String getString(Charset charset, int bufferLen);

    Buffer putString(String s, Charset charset);

    String getString(int index, Charset charset, int bufferLen);

    Buffer putString(int index, String s, Charset charset);

    int indexOf(byte[] b);

}
