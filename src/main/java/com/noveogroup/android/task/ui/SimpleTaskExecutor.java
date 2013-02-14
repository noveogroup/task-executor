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

package com.noveogroup.android.task.ui;

import com.noveogroup.android.task.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SimpleTaskExecutor<E extends TaskEnvironment> extends AbstractTaskExecutor<E> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(7);
    private final HashSet<TaskHandler<? extends Task, E>> queue = new HashSet<TaskHandler<? extends Task, E>>();
    private final Set<Set<Object>> interruptedTags = new HashSet<Set<Object>>();

    public void interrupt(Collection<Object> tags) {
        synchronized (lock()) {
            interruptedTags.add(new HashSet<Object>(tags));
        }
    }

    public boolean isInterrupted(Collection<Object> tags) {
        synchronized (lock()) {
            for (Iterator<Set<Object>> iterator = interruptedTags.iterator(); iterator.hasNext(); ) {
                Set<Object> interruptedTag = iterator.next();
                if (tags.containsAll(interruptedTag)) {
                    if (queue().sub(interruptedTag).isEmpty()) {
                        iterator.remove();
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    protected abstract <T extends Task> E createTaskEnvironment(TaskHandler<T, E> taskHandler, Pack args);

    private <T extends Task<T, E>> TaskHandler<T, E> execute(T task, TaskSet<E> owner, Collection<Object> tags, Pack args, TaskListener... taskListeners) {
        final AbstractTaskHandler<T, E> taskHandler = new AbstractTaskHandler<T, E>(lock(), owner, task, tags, copyTaskListeners(taskListeners)) {
        };
        taskHandler.setTaskEnvironment(createTaskEnvironment(taskHandler, args));

        synchronized (lock()) {
            if (isShutdown() || isInterrupted(owner.tags())) {
                taskHandler.interrupt();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        taskHandler.onCancel();
                        taskHandler.notifyJoinObject();
                    }
                });
            } else {
                synchronized (lock()) {
                    queue.add(taskHandler);
                }
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        taskHandler.onQueueInsert();
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                if (taskHandler.isInterrupted()) {
                                    taskHandler.onCancel();
                                } else {
                                    taskHandler.onExecute();
                                }
                                synchronized (lock()) {
                                    queue.remove(taskHandler);
                                }
                                taskHandler.onQueueRemove();
                                taskHandler.notifyJoinObject();
                            }
                        });
                    }
                });
            }
        }

        return taskHandler;
    }

    public <T extends Task> TaskHandler<T, E> execute(T task, Collection<Object> tags, Pack args, TaskListener... taskListeners) {
        HashSet<Object> tagSet = new HashSet<Object>();
        tagSet.addAll(tags);
        tagSet.add(new Object());
        SimpleTaskSet owner = new SimpleTaskSet(tagSet);
        return execute(task, owner, tagSet, args, taskListeners);
    }

    @Override
    public TaskSet<E> queue() {
        return new SimpleTaskSet(Collections.emptySet());
    }

    public class SimpleTaskSet extends AbstractTaskSet<E> {

        public SimpleTaskSet(Set<Object> tags) {
            super(tags);
        }

        @Override
        public TaskSet sub(Collection<Object> tags) {
            HashSet<Object> newTags = new HashSet<Object>(tags());
            newTags.addAll(tags);
            return new SimpleTaskSet(newTags);
        }

        @Override
        public void interrupt() {
            SimpleTaskExecutor.this.interrupt(tags());
            super.interrupt();
        }

        @Override
        public <T extends Task> TaskHandler<T, E> execute(T task, Pack args, TaskListener... taskListeners) {
            return SimpleTaskExecutor.this.execute(task, this, tags(), args, taskListeners);
        }

        @Override
        public Iterator<TaskHandler<? extends Task, E>> iterator() {
            HashSet<TaskHandler<? extends Task, E>> queueCopy;
            synchronized (lock()) {
                queueCopy = new HashSet<TaskHandler<? extends Task, E>>(queue);
            }
            for (Iterator<TaskHandler<? extends Task, E>> iterator = queueCopy.iterator(); iterator.hasNext(); ) {
                TaskHandler taskHandler = iterator.next();
                if (!taskHandler.tags().containsAll(tags())) {
                    iterator.remove();
                }
            }
            return Collections.unmodifiableSet(queueCopy).iterator();
        }

    }

}
