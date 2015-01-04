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
package net.sf.cindy.session.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;

import net.sf.cindy.Buffer;
import net.sf.cindy.Packet;
import net.sf.cindy.SessionType;
import net.sf.cindy.buffer.BufferFactory;
import net.sf.cindy.packet.DefaultPacket;
import net.sf.cindy.session.nio.reactor.ReactorHandler;
import net.sf.cindy.util.ChannelUtils;

/**
 * Pipe session.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class PipeSession extends AbstractChannelSession {

    private Pipe pipe;

    /**
     * Set the pipe which the session will used.
     * 
     * @param pipe
     *            pipe
     * @throws IllegalStateException
     */
    public void setPipe(Pipe pipe) {
        if (isStarted())
            throw new IllegalStateException(
                    "can't set pipe after session started");
        this.pipe = pipe;
    }

    /**
     * Get pipe associted with the session.
     * 
     * @return pipe
     */
    public Pipe getPipe() {
        return pipe;
    }

    public SessionType getSessionType() {
        return SessionType.PIPE;
    }

    public SocketAddress getLocalAddress() {
        return null;
    }

    public SocketAddress getRemoteAddress() {
        return null;
    }

    protected ReactorHandler getReactorHandler() {
        return new ChannelReactorHandler() {

            public SelectableChannel[] getChannels() {
                return new SelectableChannel[] { pipe.sink(), pipe.source() };
            }

            protected void read() throws IOException {
                Buffer buffer = BufferFactory.allocate(getReadPacketSize());
                SourceChannel source = pipe.source();
                int n = -1;
                int readCount = 0;

                try {
                    while ((n = buffer.read(source)) >= 0) {
                        if (n == 0)
                            break;
                        readCount += n;
                    }
                } catch (IOException e) {
                    buffer.release();
                    throw e;
                }

                if (readCount > 0) {
                    buffer.flip();
                    getSessionFilterChain(false).packetReceived(
                            new DefaultPacket(buffer));
                }
                if (n < 0) // Connection closed
                    throw new ClosedChannelException();
            }

            protected boolean write(Packet packet) throws IOException {
                Buffer buffer = packet.getContent();
                SinkChannel sink = pipe.sink();
                while (true) {
                    int n = buffer.write(sink);
                    if (!buffer.hasRemaining())
                        return true;
                    else if (n == 0) {
                        // have more data, but the kennel buffer
                        // is full, wait next time to write
                        return false;
                    }
                }
            }

        };
    }

    protected void doStart() throws IOException {
        if (pipe == null) {
            try {
                pipe = Pipe.open();
            } catch (IOException e) {
                doClose();
                throw e;
            }
        }
    }

    protected void doClose() {
        if (pipe != null) {
            ChannelUtils.close(pipe.sink());
            ChannelUtils.close(pipe.source());
            pipe = null;
        }
    }
}
