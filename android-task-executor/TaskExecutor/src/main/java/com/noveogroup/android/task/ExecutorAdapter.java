package com.noveogroup.android.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ExecutorAdapter extends AbstractExecutorService {

    private static class TaskRunnable implements Task {

        private final Runnable runnable;

        private TaskRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        @Override
        public Object run(Object input, TaskEnvironment env) throws Throwable {
            runnable.run();
            return null;
        }

    }

    private final TaskExecutor executor;
    private volatile boolean shutdown = false;

    public ExecutorAdapter(TaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("executor service is shut down");
        } else {
            executor.execute(new TaskRunnable(command));
        }
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        synchronized (executor.lock()) {
            List<Runnable> list = new ArrayList<Runnable>();
            for (TaskHandler<?, ?> handler : executor.queue()) {
                if (handler.getState() != TaskHandler.State.STARTED) {
                    TaskRunnable task = (TaskRunnable) handler.task();
                    list.add(task.getRunnable());
                    handler.interrupt();
                }
            }
            shutdown = true;
            return list;
        }
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && executor.queue().isEmpty();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long time = unit.convert(timeout, TimeUnit.MILLISECONDS);
        TaskSet queue = executor.queue();
        queue.join(time);
        return isTerminated();
    }

}
