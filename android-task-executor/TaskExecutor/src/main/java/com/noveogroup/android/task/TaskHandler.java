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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface TaskHandler<Input, Output> {

    /**
     * Represents a task's processing state. A given task may only be in one
     * state at a time.
     */
    public enum State {

        /**
         * The task has been created, but hasn't been started yet.
         */
        CREATED,

        /**
         * The task is running now.
         */
        STARTED,

        /**
         * The task has been canceled before method {@link Task#run(TaskEnvironment)}
         * was entered.
         */
        CANCELED,

        /**
         * The task was running but failed due to an exception was thrown
         * from {@link Task#run(TaskEnvironment)}.
         * <p/>
         * Task is considered as failed even if {@link InterruptedException}
         * was thrown.
         */
        FAILED,

        /**
         * The task has been started and finished successfully.
         */
        SUCCEED;

        public static final Set<State> ALIVE_SET;
        public static final Set<State> DESTROYED_SET;
        public static final Set<State> FINISHED_SET;

        static {
            Set<State> set;

            set = new HashSet<State>();
            set.add(CREATED);
            set.add(STARTED);
            ALIVE_SET = Collections.unmodifiableSet(set);

            set = new HashSet<State>();
            set.add(CANCELED);
            set.add(FAILED);
            set.add(SUCCEED);
            DESTROYED_SET = Collections.unmodifiableSet(set);

            set = new HashSet<State>();
            set.add(FAILED);
            set.add(SUCCEED);
            FINISHED_SET = Collections.unmodifiableSet(set);
        }

        /**
         * Returns {@code boolean} indicating whether the task's state is
         * alive {@code true} or {@code false}.
         *
         * @return a {@code boolean} indicating if state is {@link #CREATED}
         * or {@link #STARTED}.
         */
        public boolean isAlive() {
            return ALIVE_SET.contains(this);
        }

        /**
         * Returns {@code boolean} indicating whether the task's state is
         * destroyed {@code true} or {@code false}.
         *
         * @return a {@code boolean} indicating if state is {@link #CANCELED},
         * {@link #FAILED} or {@link #SUCCEED}.
         */
        public boolean isDestroyed() {
            return DESTROYED_SET.contains(this);
        }

        /**
         * Returns {@code boolean} indicating whether the task's state is
         * finished {@code true} or {@code false}.
         *
         * @return a {@code boolean} indicating if state is {@link #FAILED}
         * or {@link #SUCCEED}.
         */
        public boolean isFinished() {
            return FINISHED_SET.contains(this);
        }

    }

    public TaskExecutor executor();

    public TaskSet owner();

    public Object lock();

    public Task<Input, Output> task();

    public Pack<Input, Output> args();

    public State getState();

    public Throwable getThrowable();

    public boolean isInterrupted();

    public void interrupt();

    public void join() throws InterruptedException;

    public boolean join(long timeout) throws InterruptedException;

}
