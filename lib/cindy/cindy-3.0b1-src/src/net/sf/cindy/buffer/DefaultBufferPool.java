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

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TreeMap;

import net.sf.cindy.Buffer;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation of <code>BufferPool</code>.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class DefaultBufferPool implements BufferPool {

    private static class Entry {

        private final Object obj;
        private Entry next;

        public Entry(Object obj) {
            assert obj != null;
            this.obj = obj;
        }

    }

    /**
     * If length less than min cachable length, we perfer re-allocate it to pool
     * it.
     */
    private static final int MIN_CACHABLE_LENGTH = 512;
    private static final int MAX_CACHABLE_LENGTH = Integer.MAX_VALUE;

    private static final int POSITIVE_INTEGER_SIZE = 31;

    private final SoftReference[] directPools = new SoftReference[POSITIVE_INTEGER_SIZE];
    private final SoftReference[] heapPools = new SoftReference[POSITIVE_INTEGER_SIZE];

    private final Lock[] directLocks = new ReentrantLock[POSITIVE_INTEGER_SIZE];
    private final Lock[] heapLocks = new ReentrantLock[POSITIVE_INTEGER_SIZE];

    private final AtomicInteger getCount = new AtomicInteger();
    private final AtomicInteger hitCount = new AtomicInteger();

    public DefaultBufferPool() {
        for (int i = 0; i < POSITIVE_INTEGER_SIZE; i++) {
            directLocks[i] = new ReentrantLock();
            heapLocks[i] = new ReentrantLock();
        }
    }

    private final int indexFor(int capacity) {
        if (capacity > MAX_CACHABLE_LENGTH || capacity < MIN_CACHABLE_LENGTH)
            return -1;
        for (int i = POSITIVE_INTEGER_SIZE - 1; i > 0; i--) {
            if ((capacity >>> i) > 0)
                return i;
        }
        return 0;
    }

    private final TreeMap getPool(SoftReference reference) {
        return reference == null ? null : (TreeMap) reference.get();
    }

    private final TreeMap createPool(SoftReference[] references, int index) {
        SoftReference reference = references[index];
        TreeMap pool = reference == null ? null : (TreeMap) reference.get();
        if (pool == null) {
            pool = new TreeMap();
            references[index] = new SoftReference(pool);
        }
        return pool;
    }

    /**
     * Heap buffer.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private final class HeapBuffer extends ByteArrayBuffer {

        private byte[] content;

        private HeapBuffer(byte[] content, int capacity) {
            super(content, 0, capacity);
            this.content = content;
        }

        private HeapBuffer(int capacity) {
            this(new byte[capacity], capacity);
        }

        protected void _release() {
            int index = indexFor(content.length);
            if (index >= 0) {
                Lock lock = heapLocks[index];
                Integer key = new Integer(content.length);
                Entry value = new Entry(content);

                lock.lock();
                try {
                    value.next = (Entry) createPool(heapPools, index).put(key,
                            value);
                } finally {
                    lock.unlock();
                }
            }
            content = null;
            super._release();
        }
    }

    /**
     * Direct buffer.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private final class DirectBuffer extends ByteBufferBuffer {

        private ByteBuffer content;

        private DirectBuffer(ByteBuffer content, int capacity) {
            super(content, 0, capacity);
            this.content = content;
        }

        private DirectBuffer(int capacity) {
            this(ByteBuffer.allocateDirect(capacity), capacity);
        }

        protected void _release() {
            int index = indexFor(content.capacity());
            if (index >= 0) {
                Lock lock = directLocks[index];
                Integer key = new Integer(content.capacity());
                Entry value = new Entry(content);

                lock.lock();
                try {
                    value.next = (Entry) createPool(directPools, index).put(
                            key, value);
                } finally {
                    lock.unlock();
                }
            }
            content = null;
            super._release();
        }

    }

    public Buffer allocate(int capacity, boolean direct) {
        getCount.incrementAndGet();

        int index = indexFor(capacity);
        if (index >= 0) {
            Integer key = new Integer(capacity);
            Object obj = get(direct, index, key);
            if (obj == null && ++index != POSITIVE_INTEGER_SIZE)
                obj = get(direct, index, key);

            if (obj != null) {
                hitCount.incrementAndGet();
                if (direct) {
                    ByteBuffer content = (ByteBuffer) obj;
                    content.clear();
                    return new DirectBuffer(content, capacity);
                } else {
                    return new HeapBuffer((byte[]) obj, capacity);
                }
            }
        }

        try {
            return allocateNew(capacity, direct);
        } catch (OutOfMemoryError e) {
            clear();
            return allocateNew(capacity, direct);
        }
    }

    private Object get(boolean direct, int index, Object key) {
        Lock lock = direct ? directLocks[index] : heapLocks[index];
        lock.lock();
        try {
            TreeMap pool = getPool(direct ? directPools[index]
                    : heapPools[index]);
            if (pool != null) {
                for (Iterator iter = pool.tailMap(key).values().iterator(); iter
                        .hasNext();) {
                    Entry first = (Entry) iter.next();
                    Entry next = first.next;
                    if (next != null) {
                        first.next = next.next;
                        next.next = null;
                        return next.obj;
                    } else {
                        iter.remove();
                        return first.obj;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    private Buffer allocateNew(int capacity, boolean direct) {
        return direct ? (Buffer) new DirectBuffer(capacity) : new HeapBuffer(
                capacity);
    }

    public void clear() {
        for (int i = 0; i < POSITIVE_INTEGER_SIZE; i++) {
            clear(heapLocks[i], heapPools[i]);
            clear(directLocks[i], directPools[i]);
        }
    }

    private void clear(Lock lock, SoftReference reference) {
        lock.lock();
        try {
            TreeMap pool = getPool(reference);
            if (pool != null)
                pool.clear();
        } finally {
            lock.unlock();
        }
    }

    public double getHitRate() {
        return (double) hitCount.get() / getCount.get();
    }

    public String toString() {
        return super.toString() + " [hitRate] " + getHitRate();
    }

}
