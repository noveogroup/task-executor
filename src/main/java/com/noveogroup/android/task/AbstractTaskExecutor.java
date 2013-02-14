/*
 * Copyright (c) 2013 Noveo Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Except as contained in this notice, the name(s) of the above copyright holders
 * shall not be used in advertising or otherwise to promote the sale, use or
 * other dealings in this Software without prior written authorization.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.noveogroup.android.task;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractTaskExecutor<E extends TaskEnvironment> implements TaskExecutor<E> {

    private final Object lock = new Object();
    private final ArrayList<TaskListener> listeners = new ArrayList<TaskListener>(8);
    private volatile boolean shutdown = false;

    @Override
    public void addTaskListener(TaskListener... taskListeners) {
        synchronized (lock()) {
            for (TaskListener taskListener : taskListeners) {
                if (taskListener != null) {
                    listeners.add(listeners.size(), taskListener);
                }
            }
        }
    }

    @Override
    public void removeTaskListener(TaskListener... taskListeners) {
        synchronized (lock()) {
            for (TaskListener taskListener : taskListeners) {
                if (taskListener != null) {
                    int lastIndex = listeners.lastIndexOf(taskListener);
                    if (lastIndex != -1) {
                        listeners.remove(lastIndex);
                    }
                }
            }
        }
    }

    protected TaskListener[] copyTaskListeners(TaskListener... addTaskListeners) {
        synchronized (lock()) {
            TaskListener[] array = new TaskListener[listeners.size() + addTaskListeners.length];
            listeners.toArray(array);
            System.arraycopy(addTaskListeners, 0, array, listeners.size(), addTaskListeners.length);
            return array;
        }
    }

    @Override
    public <T extends Task> TaskHandler<T, E> execute(T task, Collection<Object> tags, TaskListener... taskListeners) {
        return execute(task, tags, new Pack(), taskListeners);
    }

    @Override
    public <T extends Task> TaskHandler<T, E> execute(T task, Pack args, TaskListener... taskListeners) {
        return execute(task, Collections.emptySet(), args, taskListeners);
    }

    @Override
    public <T extends Task> TaskHandler<T, E> execute(T task, TaskListener... taskListeners) {
        return execute(task, Collections.emptySet(), new Pack(), taskListeners);
    }

    @Override
    public Object lock() {
        return lock;
    }

    @Override
    public void shutdown() {
        synchronized (lock()) {
            shutdown = true;
            queue().interrupt();
        }
    }

    @Override
    public boolean isShutdown() {
        synchronized (lock()) {
            return shutdown;
        }
    }

    @Override
    public boolean isTerminated() {
        synchronized (lock) {
            return shutdown && queue().size() <= 0;
        }
    }

    @Override
    public boolean awaitTermination(long timeout) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }

        // await shutdown
        synchronized (lock) {
            while (!shutdown) {
                if (timeout == 0) {
                    lock.wait();
                } else {
                    long time = SystemClock.uptimeMillis();
                    lock.wait(timeout);
                    timeout -= SystemClock.uptimeMillis() - time;

                    if (!shutdown || timeout <= 0) {
                        return false;
                    }
                }
            }
        }

        // await all threads destroyed
        while (true) {
            TaskSet taskSet = queue();
            if (taskSet.size() <= 0) {
                return true;
            } else {
                if (timeout == 0) {
                    taskSet.join();
                } else {
                    long time = SystemClock.uptimeMillis();
                    taskSet.join(timeout);
                    timeout -= SystemClock.uptimeMillis() - time;

                    if (timeout <= 0) {
                        return false;
                    }
                }
            }
        }
    }

}