package com.noveogroup.android.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ExecutorAdapter<E extends TaskEnvironment> extends AbstractExecutorService {

    private static class TaskRunnable<E extends TaskEnvironment> implements Task<E> {

        private final Runnable runnable;

        private TaskRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        @Override
        public void run(E env) throws Throwable {
            runnable.run();
        }

    }

    private final TaskExecutor<E> executor;
    private volatile boolean shutdown = false;

    public ExecutorAdapter(TaskExecutor<E> executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("executor service is shut down");
        } else {
            executor.execute(new TaskRunnable<E>(command));
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
            for (TaskHandler<?, E> handler : executor.queue()) {
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
        TaskSet<E> queue = executor.queue();
        queue.join(time);
        return isTerminated();
    }

}
