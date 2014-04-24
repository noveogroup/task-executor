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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link AbstractTaskSet} is an abstract implementation of
 * the {@link TaskSet} interface. A subclass must implement the abstract
 * methods {@link #iterator()}, {@link #interrupt()} and
 * {@link #isInterrupted()}.
 *
 * @param <E> task environment type.
 */
abstract class AbstractTaskSet<E extends TaskEnvironment> implements TaskSet<E> {

    private final TaskExecutor<E> executor;
    private final Set<String> tags;

    public AbstractTaskSet(TaskExecutor<E> executor, Collection<String> tags) {
        this.executor = executor;
        this.tags = Collections.unmodifiableSet(new HashSet<String>(tags));
    }

    @Override
    public TaskExecutor<E> executor() {
        return executor;
    }

    @Override
    public Object lock() {
        return executor().lock();
    }

    @Override
    public Set<String> tags() {
        return tags;
    }

    @Override
    public TaskSet<E> sub(String... tags) {
        return sub(Arrays.asList(tags));
    }

    @Override
    public TaskSet<E> sub(Collection<String> tags) {
        HashSet<String> tagSet = new HashSet<String>(tags());
        tagSet.addAll(tags);
        return executor().queue(tagSet);
    }

    @Override
    public <T extends Task<? super E>> TaskHandler<T, E> execute(T task, Pack args, TaskListener... taskListeners) {
        return executor().execute(task, args, Arrays.asList(taskListeners), tags());
    }

    @Override
    public <T extends Task<? super E>> TaskHandler<T, E> execute(T task, TaskListener... taskListeners) {
        return execute(task, new Pack(), taskListeners);
    }

    @Override
    public int size() {
        int size = 0;
        for (TaskHandler ignored : this) {
            size++;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public abstract Iterator<TaskHandler<? extends Task, E>> iterator();

    @Override
    public abstract boolean isInterrupted();

    @Override
    public abstract void interrupt();

    @Override
    public void join() throws InterruptedException {
        join(0);
    }

    @Override
    public boolean join(long timeout) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }

        while (true) {
            Iterator<TaskHandler<? extends Task, E>> iterator = this.iterator();
            if (!iterator.hasNext()) {
                return true;
            }
            while (iterator.hasNext()) {
                TaskHandler taskHandler = iterator.next();
                if (timeout == 0) {
                    taskHandler.join();
                } else {
                    long time = SystemClock.uptimeMillis();
                    taskHandler.join(timeout);
                    timeout -= SystemClock.uptimeMillis() - time;

                    if (timeout <= 0) {
                        return false;
                    }
                }
            }
        }
    }

}
