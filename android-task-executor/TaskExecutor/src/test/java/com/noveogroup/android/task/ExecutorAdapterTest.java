package com.noveogroup.android.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorAdapterTest {

    private static ExecutorService createAdapter() {
        SimpleTaskExecutor<SimpleTaskEnvironment> executor = new SimpleTaskExecutor<SimpleTaskEnvironment>(createExecutor()) {
            @Override
            protected <T extends Task> SimpleTaskEnvironment createTaskEnvironment(TaskHandler<T, SimpleTaskEnvironment> taskHandler) {
                return new SimpleTaskEnvironment<SimpleTaskEnvironment>(taskHandler);
            }
        };
        return new ExecutorAdapter<SimpleTaskEnvironment>(executor);
    }

    private static ExecutorService createExecutor() {
        return new ScheduledThreadPoolExecutor(3);
    }

    private void testExecute(ExecutorService executorService) {
        final StringBuffer buffer = new StringBuffer();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                buffer.append("[runnable]");
            }
        });

        Utils.doSleep(5);

        Assert.assertEquals("[runnable]", buffer.toString());
    }

    @Test
    public void adapterTestExecute() {
        testExecute(createAdapter());
    }

    @Test
    public void executorTestExecute() {
        testExecute(createExecutor());
    }

    private void testShutdown(ExecutorService executorService) {
        final StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 5; i++) {
            final char index = "ABCDE".charAt(i);
            final int time = 10 + 5 * i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    buffer.append(String.format("[%s-1]", index));
                    Utils.doSleep(time);
                    buffer.append(String.format("[%s-2]", index));
                }
            });
            Utils.doSleep(1);
        }

        Utils.doSleep(1);

        Assert.assertEquals(false, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        executorService.shutdown();
        buffer.append("[shutdown]");

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                }
            });
            Assert.fail("should throw RejectedExecutionException");
        } catch (RejectedExecutionException ignored) {
        }

        Utils.doSleep(50);
        Assert.assertEquals(true, executorService.isTerminated());

        Assert.assertEquals("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1][C-2][D-2][E-2]", buffer.toString());
    }

    @Test
    public void adapterTestShutdown() {
        testShutdown(createAdapter());
    }

    @Test
    public void executorTestShutdown() {
        testShutdown(createExecutor());
    }

    private void testShutdownNow(ExecutorService executorService) {
        final StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 5; i++) {
            final char index = "ABCDE".charAt(i);
            final int time = 10 + 5 * i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    buffer.append(String.format("[%s-1]", index));
                    Utils.doSleep(time);
                    buffer.append(String.format("[%s-2]", index));
                }

                @Override
                public String toString() {
                    return "[" + index + "]";
                }
            });
            Utils.doSleep(1);
        }

        Utils.doSleep(1);

        Assert.assertEquals(false, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        List<Runnable> list = executorService.shutdownNow();
        Assert.assertEquals(2, list.size());
        buffer.append("[shutdown]");

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());

        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                }
            });
            Assert.fail("should throw RejectedExecutionException");
        } catch (RejectedExecutionException ignored) {
        }

        Utils.doSleep(20);
        Assert.assertEquals(true, executorService.isTerminated());

        Assert.assertEquals("[A-1][B-1][C-1][shutdown][A-2][B-2][C-2]", buffer.toString());
    }

    @Test
    public void adapterTestShutdownNow() {
        testShutdownNow(createAdapter());
    }

    @Test
    public void executorTestShutdownNow() {
        testShutdownNow(createExecutor());
    }

    private void testAwaitTermination(ExecutorService executorService) throws InterruptedException {
        final StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 5; i++) {
            final char index = "ABCDE".charAt(i);
            final int time = 10 + 5 * i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    buffer.append(String.format("[%s-1]", index));
                    Utils.doSleep(time);
                    buffer.append(String.format("[%s-2]", index));
                }
            });
            Utils.doSleep(1);
        }

        Utils.doSleep(1);
        executorService.shutdown();
        buffer.append("[shutdown]");

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        Assert.assertEquals("[A-1][B-1][C-1][shutdown]", buffer.toString());
        Assert.assertEquals(false, executorService.awaitTermination(2 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        Assert.assertEquals("[A-1][B-1][C-1][shutdown]", buffer.toString());
        Assert.assertEquals(false, executorService.awaitTermination(10 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        Assert.assertEquals("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1]", buffer.toString());
        Assert.assertEquals(false, executorService.awaitTermination(20 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(false, executorService.isTerminated());
        Assert.assertEquals("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1][C-2][D-2]", buffer.toString());
        Assert.assertEquals(true, executorService.awaitTermination(20 * Utils.DT, TimeUnit.MILLISECONDS));

        Assert.assertEquals(true, executorService.isShutdown());
        Assert.assertEquals(true, executorService.isTerminated());
        Assert.assertEquals("[A-1][B-1][C-1][shutdown][A-2][D-1][B-2][E-1][C-2][D-2][E-2]", buffer.toString());
        Assert.assertEquals(true, executorService.awaitTermination(20 * Utils.DT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void adapterTestAwaitTermination() throws InterruptedException {
        testAwaitTermination(createAdapter());
    }

    @Test
    public void executorTestAwaitTermination() throws InterruptedException {
        testAwaitTermination(createExecutor());
    }

}
