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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;
import net.sf.cindy.Buffer;
import net.sf.cindy.util.Charset;

/**
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public abstract class BufferTest extends TestCase {

    protected final Random random = new Random();

    protected abstract Buffer newBuffer(int capacity);

    public void testCreate() {
        for (int i = 0; i < 5; i++) {
            int capacity = random.nextInt(1000) + 1;
            Buffer buffer = newBuffer(capacity);
            assertEquals(0, buffer.position());
            assertEquals(capacity, buffer.limit());
            assertEquals(capacity, buffer.capacity());
            assertEquals(capacity, buffer.remaining());
            assertTrue(buffer.hasRemaining());
        }
    }

    public void testRelease() {
        Buffer buffer = newBuffer(10);
        assertFalse(buffer.isReleased());
        buffer.get();

        Buffer slice = buffer.slice();
        assertFalse(slice.isReleased());
        slice.get();

        Buffer duplicate = buffer.duplicate();
        assertFalse(duplicate.isReleased());
        duplicate.get();

        buffer.release();
        assertTrue(buffer.isReleased());
        assertTrue(slice.isReleased());
        assertTrue(duplicate.isReleased());
        try {
            buffer.get();
            fail();
        } catch (ReleasedBufferException e) {
        }
        try {
            slice.get();
            fail();
        } catch (ReleasedBufferException e) {
        }
        try {
            duplicate.get();
            fail();
        } catch (ReleasedBufferException e) {
        }

        buffer = newBuffer(10);
        buffer.setPermanent(true);
        buffer.release();
        assertFalse(buffer.isReleased());
        buffer.get();
    }

    public void testEquals() {
        Buffer buffer1 = newBuffer(256);
        Buffer buffer2 = newBuffer(256);
        for (int i = 0; i < 256; i++) {
            buffer1.put((byte) i);
            buffer2.put((byte) i);
        }
        assertEquals(buffer1, buffer2);
        buffer1.flip();
        assertFalse(buffer1.equals(buffer2));
        buffer2.flip();
        assertEquals(buffer1, buffer2);
    }

    public void testAsByteBuffer() {
        Buffer buffer = newBuffer(256);
        for (int i = 0; i < 256; i++) {
            buffer.put((byte) i);
        }

        buffer.clear();
        ByteBuffer b1 = buffer.asByteBuffer();
        assertEquals(buffer.position(), b1.position());
        assertEquals(buffer.limit(), b1.limit());
        assertEquals(buffer.capacity(), b1.capacity());
        while (b1.hasRemaining())
            assertEquals(b1.get(), buffer.get());

        buffer.clear();
        buffer.limit(random.nextInt(buffer.capacity()));
        buffer.position(random.nextInt(buffer.limit()));
        ByteBuffer b2 = buffer.asByteBuffer();
        assertEquals(buffer.position(), b2.position());
        assertEquals(buffer.limit(), b2.limit());
        assertEquals(buffer.capacity(), b2.capacity());
        while (b2.hasRemaining())
            assertEquals(b2.get(), buffer.get());

        buffer.clear();
        buffer.limit(random.nextInt(buffer.capacity()));
        buffer.position(random.nextInt(buffer.limit()));
        buffer = buffer.slice();
        ByteBuffer b3 = buffer.asByteBuffer();
        assertEquals(buffer.position(), b3.position());
        assertEquals(buffer.limit(), b3.limit());
        assertEquals(buffer.capacity(), b3.capacity());
        while (b3.hasRemaining())
            assertEquals(b3.get(), buffer.get());
    }

    public void testWrite() {
        byte[] b = new byte[random.nextInt(5000)];

        Buffer buffer = newBuffer(b.length);
        buffer.put(b);
        buffer.flip();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel channel = Channels.newChannel(baos);

        try {
            int writeCount = 0;
            while (true) {
                if (!buffer.hasRemaining())
                    break;
                int n = buffer.write(channel);
                if (n == -1)
                    break;
                writeCount += n;
            }
            assertEquals(b.length, writeCount);
            assertTrue(Arrays.equals(baos.toByteArray(), b));
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testRead() {
        byte[] b = new byte[random.nextInt(5000)];
        Buffer buffer = newBuffer(b.length);

        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ReadableByteChannel channel = Channels.newChannel(bais);

        try {
            int readCount = 0;
            while (true) {
                if (!buffer.hasRemaining())
                    break;
                int n = buffer.read(channel);
                if (n == -1)
                    break;
                readCount += n;
            }
            assertEquals(b.length, readCount);
            assertEquals(buffer.position(), b.length);
            buffer.flip();

            int i = 0;
            while (buffer.hasRemaining()) {
                assertEquals(buffer.get(), b[i++]);
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    public void testSkip() {
        Buffer buffer = newBuffer(random.nextInt(100) + 10);
        buffer.skip(1);
        assertEquals(buffer.position(), 1);
        buffer.skip(2);
        assertEquals(buffer.position(), 3);
        buffer.skip(buffer.remaining());
        assertEquals(buffer.position(), buffer.limit());
    }

    public void testIndexOf() {
        Buffer buffer = newBuffer(256);
        for (int i = 0; i < 256; i++) {
            buffer.put((byte) i);
        }
        buffer.flip();

        // Test length 0
        byte[] pattern = new byte[0];
        assertEquals(0, buffer.indexOf(pattern));

        // Test length 1
        pattern = new byte[1];
        for (int i = 0; i < 10; i++) {
            random.nextBytes(pattern);
            assertEquals((int) (pattern[0] & 0xff), buffer.indexOf(pattern));
        }

        // Test length n
        for (int i = 0; i < 10; i++) {
            int pos = random.nextInt(250);
            pattern = new byte[5];
            for (int j = 0; j < pattern.length; j++) {
                pattern[j] = (byte) (pos + j);
            }
            assertEquals(pos, buffer.indexOf(pattern));
        }

        // Test wrong
        pattern = new byte[2];
        byte b = (byte) random.nextInt(256);
        pattern[0] = b;
        pattern[1] = (byte) (b - 1);
        assertEquals(-1, buffer.indexOf(pattern));

        buffer.clear();
        int randomPosition = random.nextInt(buffer.capacity());
        buffer.position(randomPosition);
        buffer = buffer.slice();
        assertEquals(0, buffer.indexOf(new byte[] { (byte) randomPosition }));
    }

    public void testDuplicate() {
        Buffer buffer = newBuffer(random.nextInt(100) + 8);
        buffer.putInt(0);
        Buffer duplicate = buffer.duplicate();
        assertEquals(buffer.position(), duplicate.position());
        assertEquals(buffer.limit(), duplicate.limit());
        assertEquals(buffer.capacity(), duplicate.capacity());

        int num = random.nextInt();
        buffer.putInt(num);
        assertEquals(8, buffer.position());
        assertEquals(4, duplicate.position());
        assertEquals(buffer.limit(), duplicate.limit());
        assertEquals(buffer.capacity(), duplicate.capacity());
        assertEquals(num, duplicate.getInt());
    }

    public void testSlice() {
        Buffer buffer = newBuffer(8);
        buffer.putInt(123);
        Buffer slice = buffer.slice();
        assertEquals(0, slice.position());
        assertEquals(4, slice.limit());
        assertEquals(4, slice.capacity());
        assertEquals(4, buffer.position());
        assertEquals(8, buffer.limit());
        assertEquals(8, buffer.capacity());

        int num = random.nextInt();
        buffer.putInt(num);
        assertEquals(8, buffer.position());
        assertEquals(num, slice.getInt());
        assertEquals(4, slice.position());

        buffer.flip();
        slice = buffer.slice();
        assertEquals(0, slice.position());
        assertEquals(8, slice.limit());
        assertEquals(123, slice.getInt());
        assertEquals(num, slice.getInt());
        assertFalse(slice.hasRemaining());
    }

    public void testString() {
        for (int i = 0; i < 10; i++) {
            char[] c = new char[random.nextInt(5000)];
            for (int j = 0; j < c.length; j++) {
                c[j] = (char) ('a' + random.nextInt(26));
            }
            Buffer buffer = newBuffer(c.length * 2);
            String s = new String(c);
            buffer.putString(s, Charset.UTF8);
            assertEquals(buffer.position(), s.length());

            buffer.putString(buffer.position(), s, Charset.UTF8);
            assertEquals(buffer.getString(buffer.position(), Charset.UTF8,
                    c.length), s);
            buffer.flip();
            assertEquals(buffer.getString(Charset.UTF8, c.length), s);
        }
    }

    public void testCompact() {
        Buffer buffer = newBuffer(400);

        buffer.putInt(0);
        buffer.compact();
        assertEquals(buffer.remaining(), 4);
        assertEquals(buffer.position(), 396);
        buffer.clear();

        for (int i = 0; i < 100; i++) {
            buffer.putInt(i);
        }
        buffer.flip();
        for (int i = 0; i < 100; i++) {
            assertEquals(i, buffer.getInt());
            buffer.compact();
            assertEquals(buffer.remaining(), 4);
            assertEquals(buffer.position(), 396);
            buffer.clear();
        }

        for (int i = 0; i < 256; i++) {
            buffer.put((byte) i);
        }
        while (buffer.hasRemaining())
            buffer.put((byte) 0);
        buffer.position(100);
        buffer.limit(200);

        Buffer b = buffer.slice();
        b.position(40);
        b.compact();
        assertEquals(b.position(), 60);
        assertEquals(b.limit(), 100);

        for (int i = 0; i < 60; i++) {
            assertEquals(b.get(i), (byte) (140 + i));
        }
    }

    public void testGetAndPut() {
        for (int i = 0; i < 10; i++) {
            byte[] b = new byte[random.nextInt(1000) + 100];
            random.nextBytes(b);

            Buffer buffer = newBuffer(b.length);
            buffer.put(b);
            assertEquals(b.length, buffer.position());
            buffer.flip();
            assertEquals(0, buffer.position());
            assertEquals(b.length, buffer.limit());
            assertEquals(b.length, buffer.capacity());
            assertEquals(b.length, buffer.remaining());
            assertTrue(buffer.hasRemaining());
            assertEquals(buffer.asByteBuffer(), ByteBuffer.wrap(b));

            random.nextBytes(b);
            buffer.put(ByteBuffer.wrap(b));
            assertEquals(b.length, buffer.position());
            assertEquals(b.length, buffer.limit());
            assertEquals(b.length, buffer.capacity());
            assertEquals(0, buffer.remaining());
            assertFalse(buffer.hasRemaining());
            buffer.flip();
            assertEquals(buffer.asByteBuffer(), ByteBuffer.wrap(b));
            assertEquals(0, buffer.position());
            assertEquals(b.length, buffer.limit());
            assertEquals(b.length, buffer.remaining());
            assertEquals(b.length, buffer.capacity());
            assertTrue(buffer.hasRemaining());

            Buffer b1 = newBuffer(b.length);
            random.nextBytes(b);
            b1.put(0, b);
            buffer.put(b1);
            assertEquals(b.length, buffer.position());
            assertEquals(b.length, buffer.limit());
            assertEquals(b.length, buffer.capacity());
            assertEquals(0, buffer.remaining());
            assertFalse(buffer.hasRemaining());
            buffer.flip();
            assertEquals(buffer.asByteBuffer(), ByteBuffer.wrap(b));
            assertEquals(0, buffer.position());
            assertEquals(b.length, buffer.limit());
            assertEquals(b.length, buffer.remaining());
            assertEquals(b.length, buffer.capacity());
            assertTrue(buffer.hasRemaining());

            ByteBuffer b2 = ByteBuffer.wrap(b);
            for (int j = 0; j < 100; j++) {
                int index = Math.max(0, random.nextInt(b.length) - 8);
                assertEquals(buffer.get(index), b2.get(index));
                assertEquals(buffer.getShort(index), b2.getShort(index));
                assertEquals(buffer.getChar(index), b2.getChar(index));
                assertEquals(buffer.getInt(index), b2.getInt(index));
                assertEquals(buffer.getLong(index), b2.getLong(index));
                assertEquals(new Float(buffer.getFloat(index)), new Float(b2
                        .getFloat(index)));
                assertEquals(new Double(buffer.getDouble(index)), new Double(b2
                        .getDouble(index)));
            }

            while (buffer.hasRemaining()) {
                assertEquals(b2.get(), buffer.get());
            }
            assertFalse(buffer.hasRemaining());
            assertEquals(b.length, buffer.position());
            assertEquals(b.length, buffer.limit());
            assertEquals(0, buffer.remaining());

            buffer.clear();
            assertEquals(0, buffer.position());
            assertEquals(b.length, buffer.limit());
            assertEquals(b.length, buffer.remaining());

            for (int j = 0; j < b.length; j++) {
                buffer.put(b[j]);
                assertEquals(j + 1, buffer.position());
                assertEquals(b.length, buffer.limit());
                assertEquals(b.length - j - 1, buffer.remaining());
            }

            for (int j = 0; j < 100; j++) {
                int index = Math.max(0, random.nextInt(b.length) - 8);
                short s = (short) random.nextInt();
                buffer.putShort(index, s);
                assertEquals(buffer.getShort(index), s);
                int in = random.nextInt();
                buffer.putInt(index, in);
                assertEquals(buffer.getInt(index), in);
                long l = random.nextLong();
                buffer.putLong(index, l);
                assertEquals(buffer.getLong(index), l);
                float f = random.nextFloat();
                buffer.putFloat(index, f);
                assertEquals(new Float(buffer.getFloat(index)), new Float(f));
                double d = random.nextDouble();
                buffer.putDouble(index, d);
                assertEquals(new Double(buffer.getDouble(index)), new Double(d));
            }
        }

        Buffer buffer = newBuffer(256);
        for (int i = 0; i < 256; i++) {
            buffer.put((byte) i);
        }
        buffer.flip();
        for (int i = 0; i < 5; i++) {
            byte[] b = new byte[random.nextInt(buffer.capacity())];
            int start = random.nextInt(buffer.capacity() - b.length);
            buffer.get(start, b);
            for (int j = 0; j < b.length; j++) {
                assertEquals(b[j], (byte) (start + j));
            }
        }
        for (int i = 0; i < 5; i++) {
            buffer.clear();
            byte[] b = new byte[random.nextInt(buffer.capacity())];
            int start = random.nextInt(buffer.capacity() - b.length);
            buffer.position(start);
            buffer.get(b);
            assertEquals(buffer.position(), start + b.length);
            for (int j = 0; j < b.length; j++) {
                assertEquals(b[j], (byte) (start + j));
            }
        }
    }

}
