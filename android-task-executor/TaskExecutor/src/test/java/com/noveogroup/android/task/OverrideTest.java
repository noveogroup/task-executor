package com.noveogroup.android.task;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverrideTest {

    public static class LogEnvironment<E extends LogEnvironment> extends SimpleTaskEnvironment<LogEnvironment<E>> {
        private final StringBuffer log;

        public LogEnvironment(TaskHandler<?, LogEnvironment<E>> handler, StringBuffer log) {
            super(handler);
            this.log = log;
        }

        public void log(String message) {
            log.append(message);
        }
    }

    public static class LogExecutor<E extends LogEnvironment> extends SimpleTaskExecutor<LogEnvironment<E>> {
        protected final StringBuffer log = new StringBuffer();

        public LogExecutor(ExecutorService executorService) {
            super(executorService);
        }

        @Override
        protected <T extends Task> LogEnvironment<E> createTaskEnvironment(TaskHandler<T, LogEnvironment<E>> taskHandler) {
            return new LogEnvironment<E>(taskHandler, log);
        }

        public String getLog() {
            return log.toString();
        }
    }

    public static class CustomEnvironment<E extends CustomEnvironment> extends LogEnvironment<CustomEnvironment<E>> {
        public CustomEnvironment(TaskHandler<?, LogEnvironment<CustomEnvironment<E>>> handler, StringBuffer log) {
            super(handler, log);
        }

        public void customMethod() {
            log("[CustomEnvironment::customMethod()]");
        }
    }

    public static class CustomExecutor<E extends CustomEnvironment> extends LogExecutor<CustomEnvironment<E>> {
        public CustomExecutor(ExecutorService executorService) {
            super(executorService);
        }

        @Override
        protected <T extends Task> LogEnvironment<CustomEnvironment<E>> createTaskEnvironment(TaskHandler<T, LogEnvironment<CustomEnvironment<E>>> taskHandler) {
            return new CustomEnvironment<E>(taskHandler, log);
        }

        public void customMethod() {
            log.append("[CustomExecutor::customMethod()]");
        }
    }

    @Test
    public void logExecutorTest() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        LogExecutor<LogEnvironment> logExecutor = new LogExecutor<LogEnvironment>(executorService);
        logExecutor.execute(new Task<LogEnvironment<LogEnvironment>>() {
            @Override
            public void run(LogEnvironment<LogEnvironment> env) throws Throwable {
                env.log("[Task::run()]");
            }
        }).join();
        Assert.assertEquals("[Task::run()]", logExecutor.getLog());
    }

    @Test
    public void customExecutorTest() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CustomExecutor<CustomEnvironment> customExecutor = new CustomExecutor<CustomEnvironment>(executorService);
        customExecutor.execute(new Task<LogEnvironment<CustomEnvironment<CustomEnvironment>>>() {
            @Override
            public void run(LogEnvironment<CustomEnvironment<CustomEnvironment>> env) throws Throwable {
                // todo strange
                ((CustomEnvironment<CustomEnvironment>) env).customMethod();
                env.log("[Task::run()]");
            }
        }).join();
        customExecutor.customMethod();
        Assert.assertEquals("[CustomEnvironment::customMethod()][Task::run()][CustomExecutor::customMethod()]", customExecutor.getLog());
    }

}
