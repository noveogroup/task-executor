package com.noveogroup.android.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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

        Utils.sleep(Utils.DT);

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
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    buffer.append(String.format("[%s-1]", index));
                    Utils.sleep(10 * Utils.DT);
                    buffer.append(String.format("[%s-2]", index));
                }
            });
            Utils.sleep(Utils.DT);
        }

        Utils.sleep(Utils.DT);

        Assert.assertEquals(false, executorService.isShutdown());

        executorService.shutdown();
        buffer.append("[shutdown]");

        Assert.assertEquals(true, executorService.isShutdown());

        try {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                }
            });
            Assert.fail("should throw RejectedExecutionException");
        } catch (RejectedExecutionException ignored) {
        }

        Utils.sleep(20 * Utils.DT);

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

}
