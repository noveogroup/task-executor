package com.noveogroup.android.task.ui;

import android.os.Handler;
import android.os.Looper;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NewUIHandlerTest extends AndroidTestCase {

    private static final long DT = 10;

    private static Set<String> set(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }

    public static void run(final Runnable runnable) {
        final RuntimeException[] exception = new RuntimeException[1];
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (RuntimeException e) {
                    exception[0] = e;
                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        if (exception[0] != null) {
            throw exception[0];
        }
    }

    public void testConstructor() {
        new NewUIHandler();
        new NewUIHandler(getContext());
        new NewUIHandler(Looper.myLooper());
        new NewUIHandler(new Handler());
    }

    public void testTags() {
        NewUIHandler uiHandler = new NewUIHandler();
        Assert.assertEquals(set(), uiHandler.tags());

        NewUIHandler subHandler = uiHandler.sub("A", "B");
        Assert.assertEquals(set("A", "B"), subHandler.tags());
        Assert.assertEquals(uiHandler, subHandler.root());

        NewUIHandler subSubHandler = subHandler.sub("C");
        Assert.assertEquals(set("A", "B", "C"), subSubHandler.tags());
        Assert.assertEquals(uiHandler, subSubHandler.root());
    }

    public void testPost1() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        builder.append("callback");
                    }
                });

                sleep(DT);

                Assert.assertEquals("callback", builder.toString());
            }
        });
    }

    public void testPost2() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.post(10 * DT, new Runnable() {
                    @Override
                    public void run() {
                        builder.append("callback");
                    }
                });

                sleep(DT);

                Assert.assertEquals("", builder.toString());
            }
        });
    }

    public void testPost3() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.sub("A").post(10 * DT, new Runnable() {
                    @Override
                    public void run() {
                        builder.append("[callback-A]");
                    }
                });

                uiHandler.sub("B").post(DT, new Runnable() {
                    @Override
                    public void run() {
                        builder.append("[callback-B]");
                    }
                });

                uiHandler.sub("C").post(new Runnable() {
                    @Override
                    public void run() {
                        builder.append("[callback-C]");
                    }
                });

                sleep(5 * DT);

                Assert.assertEquals("[callback-C][callback-B]", builder.toString());
            }
        });
    }

}
