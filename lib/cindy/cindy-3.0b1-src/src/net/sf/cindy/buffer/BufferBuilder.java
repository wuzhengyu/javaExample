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

import java.nio.ByteBuffer;

import net.sf.cindy.Buffer;

/**
 * Buffer builder.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class BufferBuilder {

    private Buffer buffer;

    public BufferBuilder() {
        this(0);
    }

    public BufferBuilder(int capacity) {
        if (capacity <= 0)
            capacity = 512;
        buffer = BufferFactory.allocate(capacity);
    }

    private void resize(int size) {
        if (buffer.remaining() < size) {
            int capacity = Math.max(buffer.position() + size, (buffer
                    .capacity() + 1) * 2);
            Buffer newBuffer = BufferFactory.allocate(capacity).put(
                    buffer.flip());
            buffer.release();
            buffer = newBuffer;
        }
    }

    public Buffer toBuffer() {
        Buffer result = BufferFactory.allocate(buffer.position());
        buffer.get(0, result);
        return result.flip();
    }

    public void release() {
        buffer.release();
    }

    public BufferBuilder append(byte b) {
        resize(1);
        buffer.put(b);
        return this;
    }

    public BufferBuilder append(short s) {
        resize(2);
        buffer.putShort(s);
        return this;
    }

    public BufferBuilder append(int i) {
        resize(4);
        buffer.putInt(i);
        return this;
    }

    public BufferBuilder append(long l) {
        resize(8);
        buffer.putLong(l);
        return this;
    }

    public BufferBuilder append(float f) {
        resize(4);
        buffer.putFloat(f);
        return this;
    }

    public BufferBuilder append(double d) {
        resize(8);
        buffer.putDouble(d);
        return this;
    }

    public BufferBuilder append(byte[] b) {
        resize(b.length);
        buffer.put(b);
        return this;
    }

    public BufferBuilder append(ByteBuffer buffer) {
        resize(buffer.remaining());
        this.buffer.put(buffer);
        return this;
    }

    public BufferBuilder append(Buffer buffer) {
        resize(buffer.remaining());
        this.buffer.put(buffer);
        return this;
    }

}
