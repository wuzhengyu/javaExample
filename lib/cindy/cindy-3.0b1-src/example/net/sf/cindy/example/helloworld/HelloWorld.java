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
package net.sf.cindy.example.helloworld;

import java.io.Serializable;

import net.sf.cindy.Future;
import net.sf.cindy.FutureListener;
import net.sf.cindy.Session;
import net.sf.cindy.SessionHandlerAdapter;
import net.sf.cindy.decoder.SerialDecoder;
import net.sf.cindy.encoder.SerialEncoder;
import net.sf.cindy.session.nio.PipeSession;

/**
 * Hello world.
 * 
 * @author <a href="chenrui@gmail.com">Roger Chen</a>
 * @version $id$
 */
public class HelloWorld {

    public static void main(String[] args) {
        // create new session
        Session session = new PipeSession();

        // set packet encoder and decoder
        session.setPacketEncoder(new SerialEncoder());
        session.setPacketDecoder(new SerialDecoder());

        // set session handler
        session.setSessionHandler(new SessionHandlerAdapter() {

            public void sessionStarted(Session session) throws Exception {
                System.out.println("session started");
            }

            public void sessionClosed(Session session) throws Exception {
                System.out.println("session closed");
            }

            public void objectReceived(Session session, Object obj)
                    throws Exception {
                System.out.println("received " + obj.getClass().getName()
                        + " : " + obj);
            }

        });

        // start session
        session.start().complete();

        // start send
        session.send("hello, world!");
        session.send(new Integer(Integer.MAX_VALUE));
        session.send(new Double(Math.random()));
        session.send(new User("User 1", 20));

        // send object and close the session
        session.send("bye!").addListener(new FutureListener() {

            public void futureCompleted(Future future) throws Exception {
                future.getSession().close();
            }
        });
    }

    private static class User implements Serializable {

        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String toString() {
            return "[name] " + name + " [age] " + age;
        }
    }
}
