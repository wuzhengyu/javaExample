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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import net.sf.cindy.buffer.DefaultBufferPool;
import net.sf.cindy.session.dispatcher.DefaultDispatcher;
import net.sf.cindy.session.nio.DatagramChannelSession;
import net.sf.cindy.session.nio.NonBlockingSessionAcceptor;
import net.sf.cindy.session.nio.PipeSession;
import net.sf.cindy.session.nio.SocketChannelSession;

/**
 * Cindy configuration.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class Configuration {

    private static final Properties PROPS = new Properties();

    static {
        // load config file
        String configFile = System.getProperty("net.sf.cindy.config",
                "cindy.properties");
        if (configFile != null) {
            InputStream is = Configuration.class
                    .getResourceAsStream(configFile);
            if (is == null)
                is = Configuration.class.getClassLoader().getResourceAsStream(
                        configFile);
            if (is == null)
                try {
                    is = new FileInputStream(configFile);
                } catch (FileNotFoundException e) {
                }

            if (is != null) {
                try {
                    PROPS.load(is);
                } catch (IOException e) {
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // load system properties
        String prefix = "net.sf.cindy.";
        for (Iterator iter = System.getProperties().entrySet().iterator(); iter
                .hasNext();) {
            Entry entry = (Entry) iter.next();
            String key = (String) entry.getKey();
            if (key.startsWith(prefix))
                PROPS.put(key.substring(prefix.length()), entry.getValue());
        }
    }

    public static String get(String key) {
        return PROPS.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }

    public static void set(String key, String value) {
        PROPS.setProperty(key, value);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value != null)
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
            }
        return defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if ("true".equalsIgnoreCase(value))
            return true;
        else if ("false".equalsIgnoreCase("value"))
            return false;
        return defaultValue;
    }

    // Global

    public static boolean isEnableJmx() {
        return getBoolean("enableJmx", false);
    }

    public static boolean isDisableInnerException() {
        return getBoolean("disableInnerException", false);
    }

    // Buffer

    public static String getBufferPool() {
        return get("buffer.bufferPool", DefaultBufferPool.class.getName());
    }

    public static boolean isUseDirectBuffer() {
        return getBoolean("buffer.useDirectBuffer", false);
    }

    public static boolean isUseLinkedBuffer() {
        return getBoolean("buffer.useLinkedBuffer", false);
    }

    // Dispatcher

    public static String getDispatcher() {
        return get("dispatcher", DefaultDispatcher.class.getName());
    }

    public static int getDispatcherConcurrent() {
        return getInt("dispatcher.concurrent", 1);
    }

    public static int getDispatcherCapacity() {
        return getInt("dispatcher.capacity", 1000);
    }

    public static int getDispatcherKeepAliveTime() {
        return getInt("dispatcher.KeepAliveTime", 5000);
    }

    // Session

    public static int getSessionTimeout() {
        return getInt("session.timeout", 0);
    }

    public static int getRecvBufferSize() {
        return getInt("session.recvBufferSize", -1);
    }

    public static int getSendBufferSize() {
        return getInt("session.sendBufferSize", -1);
    }

    public static boolean isReuseSessionAddress() {
        return getBoolean("session.reuseAddress", false);
    }

    public static boolean isTcpNoDelay() {
        return getBoolean("session.tcpNoDelay", true);
    }

    public static int getSoLinger() {
        return getInt("session.soLinger", -1);
    }

    public static int getReadPacketSize() {
        return getInt("session.readPacketSize", 8192);
    }

    public static int getWritePacketSize() {
        // NIO channel do not handle WSAENOBUFS, so we can't direct write the
        // whole packet to channel.
        return getInt("session.writePacketSize", 1024 * 1024);
    }

    public static String getTcpSession() {
        return get("session.type.tcp", SocketChannelSession.class.getName());
    }

    public static String getUdpSession() {
        return get("session.type.udp", DatagramChannelSession.class.getName());
    }

    public static String getPipeSession() {
        return get("session.type.pipe", PipeSession.class.getName());
    }

    public static String getFileSession() {
        return get("session.type.file");
    }

    // Acceptor

    public static String getTcpAcceptor() {
        return get("acceptor.type.tcp", NonBlockingSessionAcceptor.class
                .getName());
    }

    public static int getAcceptorBacklog() {
        return getInt("acceptor.backlog", 100);
    }

    public static boolean isReuseAcceptorAddress() {
        return getBoolean("acceptor.reuseAddress", false);
    }

}
