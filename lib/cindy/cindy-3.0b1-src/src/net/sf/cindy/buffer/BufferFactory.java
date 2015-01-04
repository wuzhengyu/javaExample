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
import net.sf.cindy.util.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Buffer factory.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class BufferFactory {

    private static final Log log = LogFactory.getLog(BufferFactory.class);

    /**
     * Wrap a byte array into a buffer.
     * 
     * @param array
     *            byte array
     * @return the new buffer
     */
    public static Buffer wrap(byte[] array) {
        return ByteArrayBuffer.wrap(array);
    }

    /**
     * Wrap a byte array into a buffer.
     * 
     * @param array
     *            byte array
     * @param offset
     *            the offset of the subarray to be used
     * @param length
     *            the length of the subarray to be used
     * @return the new buffer
     */
    public static Buffer wrap(byte[] array, int offset, int length) {
        return ByteArrayBuffer.wrap(array, offset, length);
    }

    /**
     * Wrap a byte buffer into a buffer.
     * 
     * @param buffer
     *            byte buffer
     * @return the new buffer
     */
    public static Buffer wrap(ByteBuffer buffer) {
        return ByteBufferBuffer.wrap(buffer);
    }

    /**
     * Wrap a buffer array into a single buffer.
     * 
     * @param buffers
     *            buffer array
     * @return the new buffer
     */
    public static Buffer wrap(Buffer[] buffers) {
        return new LinkedBuffer(buffers);
    }

    private static final BufferPool pool;
    private static final boolean useDirectBuffer = Configuration
            .isUseDirectBuffer();

    static {
        String className = Configuration.getBufferPool();
        BufferPool tempPool = null;
        if (className != null)
            try {
                tempPool = (BufferPool) Class.forName(className).newInstance();
            } catch (Exception e) {
                log.error(e);
            }
        pool = tempPool == null ? new DefaultBufferPool() : tempPool;
    }

    /**
     * Allocate a new buffer. The contents of the buffer are uninitialized, they
     * will generally be garbage.
     * 
     * @param capacity
     *            allocate capacity
     * @return the new buffer
     */
    public static Buffer allocate(int capacity) {
        return allocate(capacity, useDirectBuffer);
    }

    /**
     * Allocate a new buffer. The contents of the buffer are uninitialized, they
     * will generally be garbage.
     * 
     * @param capacity
     *            allocate capacity
     * @param direct
     *            allocate direct buffer
     * @return the new buffer
     */
    public static Buffer allocate(int capacity, boolean direct) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return pool.allocate(capacity, direct);
    }

}
