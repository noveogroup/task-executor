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

    private class SimpleTaskSet extends AbstractTaskSet<E> {

        private final List<TaskHandler<? extends Task, E>> tasks = new ArrayList<TaskHandler<? extends Task, E>>();
        private volatile List<TaskHandler<? extends Task, E>> copyTasks = null;

        public SimpleTaskSet(Collection<String> tags) {
            super(SimpleTaskExecutor.this, tags);
        }

        public void add(TaskHandler<? extends Task, E> handler) {
            synchronized (lock()) {
                tasks.add(handler);
                copyTasks = null;
            }
        }

        public void addAll(SimpleTaskSet taskSet) {
            synchronized (lock()) {
                tasks.addAll(taskSet.tasks);
                copyTasks = null;
            }
        }

        public void remove(TaskHandler<? extends Task, E> handler) {
            synchronized (lock()) {
                tasks.remove(handler);
                copyTasks = null;
            }
        }

        @Override
        public Iterator<TaskHandler<? extends Task, E>> iterator() {
            synchronized (lock()) {
                if (copyTasks == null) {
                    copyTasks = new ArrayList<TaskHandler<? extends Task, E>>(tasks);
                }
                return copyTasks.iterator();
            }
        }

        @Override
        public boolean isInterrupted() {
            return SimpleTaskExecutor.this.isTaskSetInterrupted(tags());
        }

        @Override
        public void interrupt() {
            SimpleTaskExecutor.this.interruptTaskSet(tags());
        }

    }

    private final ExecutorService executorService;
    private final Set<Set<String>> interruptedTags = new HashSet<Set<String>>();
    private final Map<Set<String>, SimpleTaskSet> queue = new HashMap<Set<String>, SimpleTaskSet>();
    private volatile int cleanCounter = 0;

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

    private void cleanExecutor() {
        synchronized (lock()) {
            // increase counter, check if cleaning is really needed
            if (cleanCounter++ < 100) {
                return;
            }

            // clean interrupted tags set
            for (Iterator<Set<String>> iterator = interruptedTags.iterator(); iterator.hasNext(); ) {
                Set<String> set = iterator.next();
                if (queue(set).isEmpty()) {
                    iterator.remove();
                }
            }

            // clean task set map
            for (Iterator<Map.Entry<Set<String>, SimpleTaskSet>> iterator = queue.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Set<String>, SimpleTaskSet> entry = iterator.next();
                if (entry.getValue().isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    private boolean isTaskSetInterrupted(Set<String> tags) {
        synchronized (lock()) {
            if (SimpleTaskExecutor.this.isShutdown()) {
                return true;
            } else {
                cleanExecutor();

                for (Iterator<Set<String>> iterator = interruptedTags.iterator(); iterator.hasNext(); ) {
                    Set<String> set = iterator.next();
                    if (tags.containsAll(set)) {
                        if (queue(set).isEmpty()) {
                            iterator.remove();
                        } else {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

    private void interruptTaskSet(Set<String> tags) {
        synchronized (lock()) {
            interruptedTags.add(tags);
            for (TaskHandler<?, E> handler : queue(tags)) {
                handler.interrupt();
            }
        }
    }

    @Override
    public TaskSet<E> queue(Collection<String> tags) {
        synchronized (lock()) {
            // get or create task set
            HashSet<String> copyTags = new HashSet<String>(tags);
            SimpleTaskSet subTaskSet = queue.get(copyTags);
            if (subTaskSet == null) {
                subTaskSet = new SimpleTaskSet(copyTags);
                queue.put(copyTags, subTaskSet);

                // add sub-tasks to this set
                for (SimpleTaskSet taskSet : queue.values()) {
                    if (taskSet.tags().containsAll(subTaskSet.tags())) {
                        subTaskSet.addAll(taskSet);
                    }
                }
            }
            return subTaskSet;
        }
    }

    private void addToQueue(TaskHandler<?, E> handler) {
        synchronized (lock()) {
            for (SimpleTaskSet taskSet : queue.values()) {
                if (handler.owner().tags().containsAll(taskSet.tags())) {
                    taskSet.add(handler);
                }
            }
        }
    }

    private void removeFromQueue(TaskHandler<?, E> handler) {
        synchronized (lock()) {
            cleanExecutor();

            for (SimpleTaskSet taskSet : queue.values()) {
                if (handler.owner().tags().containsAll(taskSet.tags())) {
                    taskSet.remove(handler);
                }
            }
        }
    }

    @Override
    public <T extends Task<E>> TaskHandler<T, E> execute(T task, Pack args, List<TaskListener> taskListeners, Collection<String> tags) {
        return new AbstractTaskHandler<T, E>(executorService, task, queue(tags), args, copyTaskListeners(taskListeners)) {
            @Override
            protected E createTaskEnvironment() {
                return SimpleTaskExecutor.this.createTaskEnvironment(this);
            }

            @Override
            protected void addToQueue() {
                SimpleTaskExecutor.this.addToQueue(this);
            }

            @Override
            protected void removeFromQueue() {
                SimpleTaskExecutor.this.removeFromQueue(this);
            }
        };
    }

}
