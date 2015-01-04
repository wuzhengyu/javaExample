package net.sf.cindy.session.dispatcher;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author Roger Chen
 */
public class DirectDispatcherTest extends TestCase {

    private void dispatch(Dispatcher dispatcher, Runnable runnable) {
        dispatcher.dispatch(null, runnable);
    }

    public void testDispatch() {
        final List list = new ArrayList();

        final Dispatcher dispatcher = new DirectDispatcher();
        dispatch(dispatcher, new Runnable() {

            public void run() {
                list.add("1");
                dispatch(dispatcher, new Runnable() {

                    public void run() {
                        list.add("3");
                        dispatch(dispatcher, new Runnable() {

                            public void run() {
                                list.add("7");
                            };
                        });
                        list.add("4");
                        dispatch(dispatcher, new Runnable() {

                            public void run() {
                                list.add("8");
                            };
                        });
                    }
                });
                list.add("2");
                dispatch(dispatcher, new Runnable() {

                    public void run() {
                        list.add("5");
                        dispatch(dispatcher, new Runnable() {

                            public void run() {
                                list.add("9");
                            };
                        });
                        list.add("6");
                        dispatch(dispatcher, new Runnable() {

                            public void run() {
                                list.add("10");
                                dispatch(dispatcher, new Runnable() {

                                    public void run() {
                                        list.add("12");
                                    };
                                });
                                list.add("11");
                            };
                        });
                    }
                });
            }
        });

        assertEquals(12, list.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(String.valueOf(i + 1), list.get(i));
        }
    }
}
