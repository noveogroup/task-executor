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

    public void testPost() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("callback");
                    }
                });

                sleep(DT);

                Assert.assertEquals("callback", buffer.toString());
            }
        });
    }

    public void testPostWithDelay1() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.post(10 * DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("callback");
                    }
                });

                sleep(DT);

                Assert.assertEquals("", buffer.toString());
            }
        });
    }

    public void testPostWithDelay2() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.sub("A").post(10 * DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callback-A]");
                    }
                });

                uiHandler.sub("B").post(DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callback-B]");
                    }
                });

                uiHandler.sub("C").post(new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callback-C]");
                    }
                });

                sleep(5 * DT);

                Assert.assertEquals("[callback-C][callback-B]", buffer.toString());
            }
        });
    }

    public void testJoin() {
        run(new Runnable() {
            @Override
            public void run() {
                try {
                    final StringBuffer buffer = new StringBuffer();

                    final NewUIHandler uiHandler = new NewUIHandler(getContext());

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            sleep(DT);
                            buffer.append("[callback]");
                        }
                    });

                    buffer.append("[method1]");
                    uiHandler.join();
                    buffer.append("[method2]");

                    Assert.assertEquals("[method1][callback][method2]", buffer.toString());
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    public void testJoinWithTags1() {
        run(new Runnable() {
            @Override
            public void run() {
                try {
                    final StringBuffer buffer = new StringBuffer();

                    final NewUIHandler uiHandler = new NewUIHandler(getContext());

                    uiHandler.sub("A").post(new Runnable() {
                        @Override
                        public void run() {
                            sleep(DT);
                            buffer.append("[callback]");
                        }
                    });

                    buffer.append("[method1]");
                    uiHandler.join();
                    buffer.append("[method2]");

                    Assert.assertEquals("[method1][callback][method2]", buffer.toString());
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    public void testJoinWithTags2() {
        run(new Runnable() {
            @Override
            public void run() {
                try {
                    final StringBuffer buffer = new StringBuffer();

                    final NewUIHandler uiHandler = new NewUIHandler(getContext());

                    uiHandler.sub("A").post(new Runnable() {
                        @Override
                        public void run() {
                            sleep(DT);
                            buffer.append("[callback]");
                        }
                    });

                    buffer.append("[method1]");
                    uiHandler.sub("B").join();
                    buffer.append("[method2]");

                    Assert.assertEquals("[method1][method2]", buffer.toString());
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    public void testRemove() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        sleep(DT);
                        buffer.append("[callback]");
                    }
                });

                uiHandler.remove();

                Assert.assertEquals("", buffer.toString());
            }
        });
    }

    public void testRemoveWithTags() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.sub("A").post(5 * DT, new Runnable() {
                    @Override
                    public void run() {
                        sleep(DT);
                        buffer.append("[callbackA]");
                    }
                });

                uiHandler.sub("B").post(5 * DT, new Runnable() {
                    @Override
                    public void run() {
                        sleep(DT);
                        buffer.append("[callbackB]");
                    }
                });

                uiHandler.sub("A").remove();

                sleep(10 * DT);

                Assert.assertEquals("[callbackB]", buffer.toString());
            }
        });
    }

    public void testRemoveWithJoin() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final NewUIHandler uiHandler = new NewUIHandler(getContext());

                uiHandler.post(DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callback]");
                    }
                });

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            uiHandler.join();
                            buffer.append("[thread]");
                        } catch (InterruptedException ignored) {
                        }
                    }
                }.start();

                uiHandler.remove();

                sleep(10 * DT);

                Assert.assertEquals("[thread]", buffer.toString());
            }
        });
    }

}
