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

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * {@link SimpleTaskEnvironment} is an default implementation of
 * the {@link TaskExecutor} interface. A subclass  must implement the abstract
 * method {@link #createTaskEnvironment(TaskHandler)}.
 *
 * @param <E> task environment type.
 */
public abstract class SimpleTaskExecutor<E extends TaskEnvironment> extends AbstractTaskExecutor<E> {

    private static class AssociationSet<V> {

        private final Map<V, Set<String>> values = new HashMap<V, Set<String>>();
        private final Set<Set<String>> interruptedTags = new HashSet<Set<String>>();

        public void add(V value, Collection<String> tags) {
            values.put(value, new HashSet<String>(tags));
        }

        public void remove(V value) {
            values.remove(value);
        }

        // todo optimize it
        public Set<V> getAssociated(Collection<String> tags) {
            Set<V> set = new HashSet<V>();
            for (Map.Entry<V, Set<String>> entry : values.entrySet()) {
                if (entry.getValue().containsAll(tags)) {
                    set.add(entry.getKey());
                }
            }
            return Collections.unmodifiableSet(set);
        }

        // todo optimize it
        public boolean isInterrupted(Collection<String> tags) {
            boolean isInterrupted = false;
            for (Iterator<Set<String>> iterator = interruptedTags.iterator(); iterator.hasNext(); ) {
                Set<String> set = iterator.next();
                if (tags.containsAll(set)) {
                    if (getAssociated(set).isEmpty()) {
                        iterator.remove();
                    } else {
                        isInterrupted = true;
                    }
                }
            }
            return isInterrupted;
        }

        public void interrupt(Collection<String> tags) {
            interruptedTags.add(new HashSet<String>(tags));
        }

    }

    private final ExecutorService executorService;
    private final AssociationSet<TaskHandler<? extends Task, E>> queue = new AssociationSet<TaskHandler<? extends Task, E>>();

    public SimpleTaskExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Creates task environment for this task.
     * <p/>
     * Created environment must use methods of this handler to implement
     * its base functionality.
     *
     * @param taskHandler corresponding {@link TaskHandler} object.
     * @param <T>         type of task.
     * @return a {@link TaskEnvironment} object.
     */
    protected abstract <T extends Task> E createTaskEnvironment(TaskHandler<T, E> taskHandler);

    @Override
    public TaskSet<E> queue(Collection<String> tags) {
        synchronized (lock()) {
            return new AbstractTaskSet<E>(this, tags) {
                @Override
                public Iterator<TaskHandler<? extends Task, E>> iterator() {
                    synchronized (lock()) {
                        return queue.getAssociated(tags()).iterator();
                    }
                }

                @Override
                public boolean isInterrupted() {
                    synchronized (lock()) {
                        if (SimpleTaskExecutor.this.isShutdown()) {
                            return true;
                        } else {
                            return queue.isInterrupted(tags());
                        }
                    }
                }

                @Override
                public void interrupt() {
                    synchronized (lock()) {
                        queue.interrupt(tags());
                        for (TaskHandler<?, E> handler : queue.getAssociated(tags())) {
                            handler.interrupt();
                        }
                    }
                }
            };
        }
    }

    @Override
    public <T extends Task<? super E>> TaskHandler<T, E> execute(T task, Pack args, List<TaskListener> taskListeners, Collection<String> tags) {
        return new AbstractTaskHandler<T, E>(executorService, task, queue(tags), args, copyTaskListeners(taskListeners)) {
            @Override
            protected E createTaskEnvironment() {
                return SimpleTaskExecutor.this.createTaskEnvironment(this);
            }

            @Override
            protected void addToQueue() {
                synchronized (lock()) {
                    queue.add(this, owner().tags());
                }
            }

            @Override
            protected void removeFromQueue() {
                synchronized (lock()) {
                    queue.remove(this);
                }
            }
        };
    }

}
