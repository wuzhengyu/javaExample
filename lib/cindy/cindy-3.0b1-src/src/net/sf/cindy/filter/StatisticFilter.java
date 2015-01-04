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
package net.sf.cindy.filter;

import net.sf.cindy.Packet;
import net.sf.cindy.Session;
import net.sf.cindy.SessionFilterAdapter;
import net.sf.cindy.SessionFilterChain;
import net.sf.cindy.util.Speed;

/**
 * Statistic filter.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class StatisticFilter extends SessionFilterAdapter {

    private final Session session;
    private final Speed received = Speed.getInstance();
    private final Speed sent = Speed.getInstance();

    private volatile boolean stopped;

    // when stopped, use these values.
    private double avgReceiveSpeed = 0;
    private double avgSendSpeed = 0;
    private long elapsedTime = 0;

    /**
     * Construct a statistic filter monitor all sessions.
     */
    public StatisticFilter() {
        this.session = null;
        stopped = false;
    }

    /**
     * Construct a statistic filter monitor special session.
     * 
     * @param session
     *            monitor special session
     */
    public StatisticFilter(Session session) {
        this.session = session;
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void start() {
        stopped = false;
    }

    public void stop() {
        avgReceiveSpeed = received.getAvgSpeed();
        avgSendSpeed = sent.getAvgSpeed();
        elapsedTime = received.getElapsedTime();
        stopped = true;
    }

    /**
     * Get average receive speed, in byte/s.
     * 
     * @return average receive speed.
     */
    public double getAvgReceiveSpeed() {
        return stopped ? avgReceiveSpeed : received.getAvgSpeed();
    }

    /**
     * Get average send speed, in byte/s.
     * 
     * @return average send speed.
     */
    public double getAvgSendSpeed() {
        return stopped ? avgSendSpeed : sent.getAvgSpeed();
    }

    /**
     * Get elapsed time after session started and before session closed, in ms.
     * 
     * @return elapsed time
     */
    public long getElapsedTime() {
        return stopped ? elapsedTime : received.getElapsedTime();
    }

    /**
     * Get received bytes count. If session restart, the value will reset to 0.
     * 
     * @return received bytes count
     */
    public long getReceivedBytes() {
        return received.getTotalValue();
    }

    /**
     * Get sent bytes count. If session restart, the value will reset to 0.
     * 
     * @return sent bytes count
     */
    public long getSentBytes() {
        return sent.getTotalValue();
    }

    /**
     * Get instantaneous receive speed, in byte/s.
     * 
     * @return received speed
     */
    public double getReceiveSpeed() {
        return stopped ? 0 : received.getSpeed();
    }

    /**
     * Get instantaneous send speed, in byte/s.
     * 
     * @return send speed
     */
    public double getSendSpeed() {
        return stopped ? 0 : sent.getSpeed();
    }

    /**
     * Reset.
     */
    public void reset() {
        avgReceiveSpeed = 0;
        avgSendSpeed = 0;
        elapsedTime = 0;
        received.reset();
        sent.reset();
    }

    public void sessionStarted(SessionFilterChain filterChain) throws Exception {
        if (session == filterChain.getSession()) {
            reset();
            start();
        }
        super.sessionStarted(filterChain);
    }

    public void sessionClosed(SessionFilterChain filterChain) throws Exception {
        if (session == filterChain.getSession()) {
            stop();
        }
        super.sessionClosed(filterChain);
    }

    public void packetReceived(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        if (session == null || session == filterChain.getSession()) {
            received.addValue(packet.getContent().remaining());
        }
        super.packetReceived(filterChain, packet);
    }

    public void packetSent(SessionFilterChain filterChain, Packet packet)
            throws Exception {
        if (session == null || session == filterChain.getSession()) {
            sent.addValue(packet.getContent().remaining());
        }
        super.packetSent(filterChain, packet);
    }

}
