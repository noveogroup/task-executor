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
 * Interface definition for a callbacks to be invoked during task lifecycle.
 * <p/>
 * The callback method can take any time to execute - it only makes the task
 * lifetime longer. If some exception is thrown from a callback it won't be
 * caught and will be reported to the standard {@link Thread.UncaughtExceptionHandler}.
 * <p/>
 * The whole set of callbacks is divided onto two subsets:
 * <ul>
 * <li>
 * Life cycle callbacks
 * <ul>
 * <li>{@link #onCreate(TaskHandler)} and {@link #onDestroy(TaskHandler)}</li>
 * <li>{@link #onQueueInsert(TaskHandler)} and {@link #onQueueRemove(TaskHandler)}</li>
 * <li>{@link #onStart(TaskHandler)} and {@link #onFinish(TaskHandler)}</li>
 * </ul>
 * </li>
 * <li>
 * Informational callbacks
 * <ul>
 * <li>{@link #onCanceled(TaskHandler)} </li>
 * <li>{@link #onFailed(TaskHandler)}</li>
 * <li>{@link #onSucceed(TaskHandler)} </li>
 * </ul>
 * </li>
 * </ul>
 * Each task can have a set of listeners to report its state to. When it is
 * needed the corresponding callback methods (the same for each listener) are
 * called is direct or reverse order, one by one. The task won't call a next
 * set of callbacks before the current is done.
 * <p/>
 * Any process of task execution passes through the following steps:
 * <ol>
 * <li>User calls one of methods that start a task execution.
 * {@link TaskExecutor} creates {@link TaskHandler} object to handle new
 * task, check if it can be executed ({@link TaskExecutor} isn't shutdown and
 * owner {@link TaskSet} isn't interrupted) and adds it to to queue if so.
 * {@link TaskExecutor} sets {@link TaskHandler.State#CREATED} as a state of
 * execution processes. After that {@link TaskExecutor} prepares callbacks of
 * listeners of just created task to be executed in separate thread.</li>
 * <li>It is possible that the user manages to interrupt the task before
 * the callbacks are executed. This case is absolutely the same as if the user
 * tried to execute task on shutdown {@link TaskExecutor} or if owner
 * {@link TaskSet} had been already interrupted. In this cases the task will be
 * removed from queue, state will be set to {@link TaskHandler.State#CANCELED}
 * and an interruption request will be sent.</li>
 * <li>When one of background working threads is ready to execute task
 * callbacks it check if the task was already interrupted. If the task is
 * interrupted (so it is not in the queue) the thread executes
 * {@link #onCreate(TaskHandler)}, {@link #onCanceled(TaskHandler)},
 * {@link #onDestroy(TaskHandler)} and finishes task processing.</li>
 * <li>If the task hasn't been interrupted the working thread executes
 * {@link #onCreate(TaskHandler)}, {@link #onQueueInsert(TaskHandler)} so
 * task becomes prepared and starts to wait to be executed.</li>
 * <li>If user cancels the task while it is waiting it will be removed from
 * the queue, its state will be set to {@link TaskHandler.State#CANCELED} and
 * {@link #onCanceled(TaskHandler)}, {@link #onQueueRemove(TaskHandler)},
 * {@link #onDestroy(TaskHandler)} will be executed in a separate thread.
 * Finally, the task processing will be finished.</li>
 * <li>If user doesn't cancel the task it will be executed after some time.
 * Initially, working thread sets state to {@link TaskHandler.State#STARTED}
 * and call {@link #onStart(TaskHandler)}. After that thread calls
 * {@link Task#run(TaskEnvironment)}.</li>
 * <li>If an exception (even {@link InterruptedException}) is thrown from
 * {@link Task#run(TaskEnvironment)} during execution - the task becomes
 * {@link TaskHandler.State#FAILED}, it will be removed from queue and
 * {@link #onFinish(TaskHandler)}, {@link #onFailed(TaskHandler)},
 * {@link #onQueueRemove(TaskHandler)}, {@link #onDestroy(TaskHandler)}
 * will be called.</li>
 * <li>If no exceptions are thrown during execution - the task becomes
 * {@link TaskHandler.State#SUCCEED}, it will be removed from queue and
 * {@link #onFinish(TaskHandler)}, {@link #onSucceed(TaskHandler)},
 * {@link #onQueueRemove(TaskHandler)}, {@link #onDestroy(TaskHandler)}
 * will be called.</li>
 * </ol>
 *
 * @see TaskListener.Default
 * @see TaskHandler.State
 */
public interface TaskListener {

    /**
     * Default implementation of {@link TaskListener}.
     * <p/>
     * Does nothing.
     */
    public class Default implements TaskListener {

        @Override
        public void onCreate(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onQueueInsert(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onStart(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onFinish(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onQueueRemove(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onDestroy(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onCanceled(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onFailed(TaskHandler<?, ?> handler) {
            // do nothing
        }

        @Override
        public void onSucceed(TaskHandler<?, ?> handler) {
            // do nothing
        }

    }

    /**
     * Should be called in <b>direct</b> order.
     *
     * @param handler the task handler.
     * @see #onDestroy(TaskHandler)
     */
    public void onCreate(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>direct</b> order.
     *
     * @param handler the task handler.
     * @see #onQueueRemove(TaskHandler)
     */
    public void onQueueInsert(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>direct</b> order.
     *
     * @param handler the task handler.
     * @see #onFinish(TaskHandler)
     */
    public void onStart(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>reverse</b> order.
     *
     * @param handler the task handler.
     * @see #onStart(TaskHandler)
     */
    public void onFinish(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>reverse</b> order.
     *
     * @param handler the task handler.
     * @see #onQueueInsert(TaskHandler)
     */
    public void onQueueRemove(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>reverse</b> order.
     *
     * @param handler the task handler.
     * @see #onCreate(TaskHandler)
     */
    public void onDestroy(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>reverse</b> order.
     *
     * @param handler the task handler.
     */
    public void onCanceled(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>reverse</b> order.
     *
     * @param handler the task handler.
     */
    public void onFailed(TaskHandler<?, ?> handler);

    /**
     * Should be called in <b>reverse</b> order.
     *
     * @param handler the task handler.
     */
    public void onSucceed(TaskHandler<?, ?> handler);

}
