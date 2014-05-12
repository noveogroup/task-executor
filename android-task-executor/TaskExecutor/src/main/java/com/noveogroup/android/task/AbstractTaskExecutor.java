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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@link AbstractTaskExecutor} is an abstract implementation of
 * the {@link TaskExecutor} interface. A subclass must implement the abstract
 * methods {@link TaskExecutor#execute(Task, Pack, List, Collection)} and
 * {@link #queue(Collection)}}.
 */
abstract class AbstractTaskExecutor implements TaskExecutor {

    private final Object lock = new Object();
    private final ArrayList<TaskListener> listeners = new ArrayList<TaskListener>(8);
    private volatile boolean shutdown = false;

    @Override
    public Object lock() {
        return lock;
    }

    @Override
    public Pack newPack() {
        return new Pack(lock());
    }

    @Override
    public Pack newPack(Pack pack) {
        return new Pack(lock(), pack);
    }

    @Override
    public TaskSet queue(String... tags) {
        return queue(Arrays.asList(tags));
    }

    @Override
    public abstract TaskSet queue(Collection<String> tags);

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

    /**
     * Returns a copy of list of already added listeners and adds
     * all of listeners from the parameter.
     *
     * @param addTaskListeners a list of additional listeners to add.
     * @return a list containing all of listeners.
     */
    protected List<TaskListener> copyTaskListeners(List<TaskListener> addTaskListeners) {
        synchronized (lock()) {
            List<TaskListener> list = new ArrayList<TaskListener>(listeners.size() + addTaskListeners.size());
            list.addAll(listeners);
            list.addAll(addTaskListeners);
            return list;
        }
    }

    @Override
    public abstract TaskHandler execute(Task task, Pack args, List<TaskListener> taskListeners, Collection<String> tags);

    @Override
    public TaskHandler execute(Task task, Pack args, List<TaskListener> taskListeners, String... tags) {
        return execute(task, args, taskListeners, Arrays.asList(tags));
    }

    @Override
    public TaskHandler execute(Task task, Pack args, TaskListener taskListener, String... tags) {
        return execute(task, args, Arrays.asList(taskListener), Arrays.asList(tags));
    }

    @Override
    public TaskHandler execute(Task task, Pack args, String... tags) {
        return execute(task, args, new ArrayList<TaskListener>(0), Arrays.asList(tags));
    }

    @Override
    public TaskHandler execute(Task task, List<TaskListener> taskListeners, String... tags) {
        return execute(task, new Pack(), taskListeners, Arrays.asList(tags));
    }

    @Override
    public TaskHandler execute(Task task, TaskListener taskListener, String... tags) {
        return execute(task, new Pack(), Arrays.asList(taskListener), Arrays.asList(tags));
    }

    @Override
    public TaskHandler execute(Task task, String... tags) {
        return execute(task, new Pack(), new ArrayList<TaskListener>(0), Arrays.asList(tags));
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

}