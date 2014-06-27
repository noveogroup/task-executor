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
import java.util.concurrent.Executors;

/**
 * {@link SimpleTaskEnvironment} is an default implementation of
 * the {@link TaskExecutor} interface. A subclass  must implement the abstract
 * method {@link #createTaskEnvironment(TaskHandler)}.
 */
public class SimpleTaskExecutor extends AbstractTaskExecutor {

    private static Set<TaskHandler<?, ?>> getAssociated(Set<TaskHandler<?, ?>> queue,
                                                        Collection<String> tags, Collection<TaskHandler.State> states) {
        Set<TaskHandler<?, ?>> set = new HashSet<TaskHandler<?, ?>>();
        for (TaskHandler<?, ?> taskHandler : queue) {
            if (taskHandler.owner().tags().containsAll(tags) && states.contains(taskHandler.getState())) {
                set.add(taskHandler);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    private final ExecutorService executorService;
    private final Set<TaskHandler<?, ?>> queue = new HashSet<TaskHandler<?, ?>>();

    public SimpleTaskExecutor() {
        this(Executors.newCachedThreadPool());
    }

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
     * @param <Input>     type of task input.
     * @param <Output>    type of task output.
     * @return a {@link TaskEnvironment} object.
     */
    protected <Input, Output> TaskEnvironment<Input, Output> createTaskEnvironment(TaskHandler<Input, Output> taskHandler) {
        return new SimpleTaskEnvironment<Input, Output>(taskHandler);
    }

    @Override
    public TaskSet queue(Collection<String> tags, Collection<TaskHandler.State> states) {
        synchronized (lock()) {
            return new AbstractTaskSet(this, tags, states) {
                @Override
                public Iterator<TaskHandler<?, ?>> iterator() {
                    synchronized (lock()) {
                        return getAssociated(queue, tags(), states()).iterator();
                    }
                }

                @Override
                public void interrupt() {
                    synchronized (lock()) {
                        for (TaskHandler<?, ?> handler : getAssociated(queue, tags(), states())) {
                            handler.interrupt();
                        }
                    }
                }
            };
        }
    }

    @Override
    public <Input, Output> TaskHandler<Input, Output> execute(Task<Input, Output> task, Pack<Input, Output> vars, List<TaskListener<Input, Output>> taskListeners, Collection<String> tags) {
        return new AbstractTaskHandler<Input, Output>(executorService, task, this, queue(tags), vars, copyTaskListeners(taskListeners)) {
            @Override
            protected TaskEnvironment<Input, Output> createTaskEnvironment() {
                return SimpleTaskExecutor.this.createTaskEnvironment(this);
            }

            @Override
            protected void addToQueue() {
                synchronized (lock()) {
                    queue.add(this);
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
