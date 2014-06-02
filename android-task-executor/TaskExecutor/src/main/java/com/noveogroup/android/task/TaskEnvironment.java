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

/**
 * Represents a task environment for {@link Task}. Each time an assigned
 * task environment is passed as a parameter to {@link Task#run(TaskEnvironment)}.
 * <p/>
 * Task environment is strictly corresponds to task execution. Even if the object
 * representing a task is reused different task environments will be passed
 * inside {@link Task#run(TaskEnvironment)} as a parameter.
 */
public interface TaskEnvironment<Input, Output> {

    /**
     * Returns a task executor that runs this task.
     *
     * @return owner {@link TaskExecutor}.
     */
    public TaskExecutor executor();

    /**
     * Returns a task set that owns a task corresponding to this task environment.
     *
     * @return owner {@link TaskSet}.
     */
    public TaskSet owner();

    /**
     * Returns a task handler corresponding to this task environment.
     *
     * @return corresponding {@link TaskHandler}.
     */
    public TaskHandler<Input, Output> handler();

    /**
     * Returns synchronization object of {@link TaskExecutor}.
     *
     * @return the synchronization object.
     */
    public Object lock();

    /**
     * Returns an object manages both input and output arguments of task
     * corresponding to this task environment.
     * <p/>
     * Access to this container can be synchronized using an object returning
     * {@link Pack#lock()} which is the same object as the global lock object
     * of task executor returning by {@link TaskExecutor#lock()}.
     *
     * @return the container of arguments.
     * @see Pack
     * @see Pack#lock()
     * @see TaskExecutor#lock()
     * @see #lock()
     */
    public Pack<Input, Output> args();

    /**
     * Posts an interrupt request to a task corresponding to this task environment.
     * Usually this method is called from inside of {@link Task#run(TaskEnvironment)}
     * and in this case an interruption flag will be set only.
     * If somehow this method is calling from outside of {@link Task#run(TaskEnvironment)},
     * it is similar to calling corresponding {@link TaskHandler#interrupt()}.
     *
     * @see #isInterrupted()
     * @see #checkInterrupted()
     * @see TaskHandler#interrupt()
     */
    public void interruptSelf();

    /**
     * Returns a {@code boolean} indicating whether the receiver has
     * an interrupt request {@code true} or {@code false}.
     *
     * @return a {@code boolean} indicating the interrupt flag.
     * @see #interruptSelf()
     * @see #checkInterrupted()
     * @see TaskHandler#isInterrupted()
     */
    public boolean isInterrupted();

    /**
     * Checks if the receiver was interrupted and throws an exception
     * if so.
     * <p/>
     * This method is recommended to use inside {@link Task#run(TaskEnvironment)}
     * in key points of execution process to support task interruption.
     *
     * @throws InterruptedException if the receiver was interrupted.
     * @see #interruptSelf()
     * @see #isInterrupted()
     */
    public void checkInterrupted() throws InterruptedException;

}
