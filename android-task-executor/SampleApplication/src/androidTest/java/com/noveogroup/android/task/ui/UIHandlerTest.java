package com.noveogroup.android.task.ui;

import android.os.Handler;
import android.os.Looper;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UIHandlerTest extends AndroidTestCase {

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
        new UIHandler();
        new UIHandler(getContext());
        new UIHandler(Looper.myLooper());
        new UIHandler(new Handler());
    }

    public void testTags() {
        UIHandler uiHandler = new UIHandler();
        Assert.assertEquals(set(), uiHandler.tags());

        UIHandler subHandler = uiHandler.sub("A", "B");
        Assert.assertEquals(set("A", "B"), subHandler.tags());
        Assert.assertEquals(uiHandler, subHandler.root());

        UIHandler subSubHandler = subHandler.sub("C");
        Assert.assertEquals(set("A", "B", "C"), subSubHandler.tags());
        Assert.assertEquals(uiHandler, subSubHandler.root());
    }

    public void testPost() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final UIHandler uiHandler = new UIHandler(getContext());

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

    public void testPostWithDelay() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final UIHandler uiHandler = new UIHandler(getContext());

                uiHandler.post(10 * DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callbackA]");
                    }
                });

                uiHandler.post(DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callbackB]");
                    }
                });

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callbackC]");
                    }
                });

                sleep(5 * DT);

                Assert.assertEquals("[callbackC][callbackB]", buffer.toString());
            }
        });
    }

    public void testPostAfterRemove() {
        run(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer();

                final UIHandler uiHandler = new UIHandler(getContext());

                uiHandler.post(DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callbackA]");
                    }
                });

                uiHandler.remove();

                uiHandler.post(DT, new Runnable() {
                    @Override
                    public void run() {
                        buffer.append("[callbackB]");
                    }
                });

                sleep(5 * DT);

                Assert.assertEquals("[callbackB]", buffer.toString());
            }
        });
    }

    public void testJoin() {
        run(new Runnable() {
            @Override
            public void run() {
                try {
                    final StringBuffer buffer = new StringBuffer();

                    final UIHandler uiHandler = new UIHandler(getContext());

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

    public void testJoinWithTags() {
        run(new Runnable() {
            @Override
            public void run() {
                try {
                    final StringBuffer buffer = new StringBuffer();

                    final UIHandler uiHandler = new UIHandler(getContext());

                    uiHandler.sub("A").post(DT, new Runnable() {
                        @Override
                        public void run() {
                            sleep(DT);
                            buffer.append("[callbackA]");
                        }
                    });

                    uiHandler.sub("B").post(new Runnable() {
                        @Override
                        public void run() {
                            sleep(DT);
                            buffer.append("[callbackB]");
                        }
                    });

                    buffer.append("[method1]");
                    uiHandler.sub("B").join();
                    buffer.append("[method2]");

                    Assert.assertEquals("[method1][callbackB][method2]", buffer.toString());
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

                final UIHandler uiHandler = new UIHandler(getContext());

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

                final UIHandler uiHandler = new UIHandler(getContext());

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

                final UIHandler uiHandler = new UIHandler(getContext());

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
