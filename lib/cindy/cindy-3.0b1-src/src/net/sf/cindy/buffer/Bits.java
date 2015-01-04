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

import net.sf.cindy.Buffer;

/**
 * Bits operate.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
class Bits {

    static Buffer encodeShort(AbstractBuffer buffer, int index, short s) {
        byte b1, b2;
        if (buffer.isBigEndian()) {
            b1 = (byte) (s >> 8);
            b2 = (byte) (s >> 0);
        } else {
            b1 = (byte) (s >> 0);
            b2 = (byte) (s >> 8);
        }
        buffer._put(index + 0, b1);
        buffer._put(index + 1, b2);
        return buffer;
    }

    static short decodeShort(AbstractBuffer buffer, int index) {
        byte b1, b2;
        if (buffer.isBigEndian()) {
            b1 = buffer._get(index + 0);
            b2 = buffer._get(index + 1);
        } else {
            b1 = buffer._get(index + 1);
            b2 = buffer._get(index + 0);
        }
        return (short) ((b1 << 8) | (b2 & 0xff));
    }

    static Buffer encodeInt(AbstractBuffer buffer, int index, int i) {
        byte b1, b2, b3, b4;
        if (buffer.isBigEndian()) {
            b1 = (byte) (i >> 24);
            b2 = (byte) (i >> 16);
            b3 = (byte) (i >> 8);
            b4 = (byte) (i >> 0);
        } else {
            b1 = (byte) (i >> 0);
            b2 = (byte) (i >> 8);
            b3 = (byte) (i >> 16);
            b4 = (byte) (i >> 24);
        }
        buffer._put(index + 0, b1);
        buffer._put(index + 1, b2);
        buffer._put(index + 2, b3);
        buffer._put(index + 3, b4);
        return buffer;
    }

    static int decodeInt(AbstractBuffer buffer, int index) {
        byte b1, b2, b3, b4;
        if (buffer.isBigEndian()) {
            b1 = buffer._get(index + 0);
            b2 = buffer._get(index + 1);
            b3 = buffer._get(index + 2);
            b4 = buffer._get(index + 3);
        } else {
            b1 = buffer._get(index + 3);
            b2 = buffer._get(index + 2);
            b3 = buffer._get(index + 1);
            b4 = buffer._get(index + 0);
        }
        return (int) ((((b1 & 0xff) << 24) | ((b2 & 0xff) << 16)
                | ((b3 & 0xff) << 8) | ((b4 & 0xff) << 0)));
    }

    static Buffer encodeLong(AbstractBuffer buffer, int index, long l) {
        byte b1, b2, b3, b4, b5, b6, b7, b8;
        if (buffer.isBigEndian()) {
            b1 = (byte) (l >> 56);
            b2 = (byte) (l >> 48);
            b3 = (byte) (l >> 40);
            b4 = (byte) (l >> 32);
            b5 = (byte) (l >> 24);
            b6 = (byte) (l >> 16);
            b7 = (byte) (l >> 8);
            b8 = (byte) (l >> 0);
        } else {
            b1 = (byte) (l >> 0);
            b2 = (byte) (l >> 8);
            b3 = (byte) (l >> 16);
            b4 = (byte) (l >> 24);
            b5 = (byte) (l >> 32);
            b6 = (byte) (l >> 40);
            b7 = (byte) (l >> 48);
            b8 = (byte) (l >> 56);
        }
        buffer._put(index + 0, b1);
        buffer._put(index + 1, b2);
        buffer._put(index + 2, b3);
        buffer._put(index + 3, b4);
        buffer._put(index + 4, b5);
        buffer._put(index + 5, b6);
        buffer._put(index + 6, b7);
        buffer._put(index + 7, b8);
        return buffer;
    }

    static long decodeLong(AbstractBuffer buffer, int index) {
        byte b1, b2, b3, b4, b5, b6, b7, b8;
        if (buffer.isBigEndian()) {
            b1 = buffer._get(index + 0);
            b2 = buffer._get(index + 1);
            b3 = buffer._get(index + 2);
            b4 = buffer._get(index + 3);
            b5 = buffer._get(index + 4);
            b6 = buffer._get(index + 5);
            b7 = buffer._get(index + 6);
            b8 = buffer._get(index + 7);
        } else {
            b1 = buffer._get(index + 7);
            b2 = buffer._get(index + 6);
            b3 = buffer._get(index + 5);
            b4 = buffer._get(index + 4);
            b5 = buffer._get(index + 3);
            b6 = buffer._get(index + 2);
            b7 = buffer._get(index + 1);
            b8 = buffer._get(index + 0);
        }
        return (((long) b1 & 0xff) << 56) | (((long) b2 & 0xff) << 48)
                | (((long) b3 & 0xff) << 40) | (((long) b4 & 0xff) << 32)
                | (((long) b5 & 0xff) << 24) | (((long) b6 & 0xff) << 16)
                | (((long) b7 & 0xff) << 8) | (((long) b8 & 0xff) << 0);
    }

}
