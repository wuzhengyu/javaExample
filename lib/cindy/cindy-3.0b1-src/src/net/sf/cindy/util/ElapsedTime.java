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
package net.sf.cindy.util;

import sun.misc.Perf;

/**
 * Elapsed time. Use nano time if possible, adjust system time may cause
 * System.currentTimeMillis() backward.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public final class ElapsedTime {

    /**
     * Provide time.
     * 
     * @author <a href="chenrui@gmail.com">Roger Chen</a>
     * @version $id$
     */
    private static interface TimeProvider {

        long getCurrentTime();

        long toMillisecond(long time);

    }

    private static class SimpleProvider implements TimeProvider {

        public long getCurrentTime() {
            return System.currentTimeMillis();
        }

        public long toMillisecond(long time) {
            return time;
        }

    }

    private static class NanoTimeProvider implements TimeProvider {

        public long getCurrentTime() {
            return System.nanoTime();
        }

        public long toMillisecond(long time) {
            return time / 1000000;
        }
    }

    private static class PerfProvider implements TimeProvider {

        private final Perf perf = Perf.getPerf();
        private final long frequency = perf.highResFrequency();

        public long getCurrentTime() {
            return perf.highResCounter();
        }

        public long toMillisecond(long time) {
            return (long) ((double) time * 1000 / frequency);
        }
    }

    private static final TimeProvider TIME_PROVIDER;

    static {
        TimeProvider provider = new SimpleProvider();

        boolean supportNanoTime = false;
        try {
            System.class.getMethod("nanoTime", new Class[0]);
            supportNanoTime = true;
        } catch (Exception e) {
        }

        if (supportNanoTime)
            provider = new NanoTimeProvider();
        else {
            try {
                Class.forName("sun.misc.Perf");
                provider = new PerfProvider();
            } catch (ClassNotFoundException e) {
            }
        }
        TIME_PROVIDER = provider;
    }

    private long startTime = TIME_PROVIDER.getCurrentTime();

    /**
     * Get elapsed time in milliseconds.
     * 
     * @return elapsed time
     */
    public long getElapsedTime() {
        return TIME_PROVIDER.toMillisecond(Math.max(0, TIME_PROVIDER
                .getCurrentTime()
                - startTime));
    }

    /**
     * Reset start time.
     * 
     * @return elapsed time
     */
    public long reset() {
        long currentTime = TIME_PROVIDER.getCurrentTime();
        long elapsedTime = TIME_PROVIDER.toMillisecond(Math.max(0, currentTime
                - startTime));
        startTime = currentTime;
        return elapsedTime;
    }
}
