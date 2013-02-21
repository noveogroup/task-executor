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
import android.util.Log;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class AbstractTaskHandler<T extends Task, E extends TaskEnvironment> implements TaskHandler<T, E> {

    private final Object joinObject = new Object();
    private final TaskSet<E> owner;
    private final T task;
    private final E env;
    private final Pack args;
    private final TaskListener[] listeners;

    private volatile State state;
    private volatile Throwable throwable;
    private volatile boolean interrupted;

    public AbstractTaskHandler(TaskSet<E> owner, T task, E env, Pack args, List<TaskListener> listeners) {
        this.owner = owner;
        this.task = task;
        this.env = env;
        this.args = args.lock() == owner.lock() ? args : new Pack(owner.lock(), args);
        this.listeners = new TaskListener[listeners.size()];
        listeners.toArray(this.listeners);

        this.state = null;
        this.throwable = null;
        this.interrupted = false;
    }

    @Override
    public TaskSet<E> owner() {
        return owner;
    }

    @Override
    public Object lock() {
        return owner().lock();
    }

    @Override
    public T task() {
        return task;
    }

    @Override
    public Pack args() {
        return args;
    }

    @Override
    public State getState() {
        synchronized (lock()) {
            return state;
        }
    }

    @Override
    public Throwable getThrowable() {
        synchronized (lock()) {
            return throwable;
        }
    }

    @Override
    public boolean isInterrupted() {
        synchronized (lock()) {
            return interrupted;
        }
    }

    @Override
    public void interrupt() {
        synchronized (lock()) {
            this.interrupted = true;
            interruptThread();
        }
    }

    /**
     * Interrupts worker thread.
     */
    protected abstract void interruptThread();

    @Override
    public void join() throws InterruptedException {
        join(0);
    }

    @Override
    public boolean join(long timeout) throws InterruptedException {
        synchronized (joinObject) {
            if (timeout < 0) {
                throw new IllegalArgumentException();
            }

            while (!getState().isDestroyed()) {
                if (timeout == 0) {
                    joinObject.wait();
                } else {
                    long time = SystemClock.uptimeMillis();
                    joinObject.wait(timeout);
                    timeout -= SystemClock.uptimeMillis() - time;

                    if (timeout <= 0) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    private void uncaughtListenerException(TaskListener listener, Throwable throwable) {
        Log.e(TaskExecutor.TAG, "listener " + listener + " failed", throwable);
        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), throwable);
    }

    private void callOnCreate() {
        for (TaskListener listener : listeners) { // in direct order
            try {
                listener.onCreate(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnQueueInsert() {
        for (TaskListener listener : listeners) { // in direct order
            try {
                listener.onQueueInsert(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnStart() {
        for (TaskListener listener : listeners) { // in direct order
            try {
                listener.onStart(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnFinish() {
        for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
            TaskListener listener = listeners[i];
            try {
                listener.onFinish(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnQueueRemove() {
        for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
            TaskListener listener = listeners[i];
            try {
                listener.onQueueRemove(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnDestroy() {
        for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
            TaskListener listener = listeners[i];
            try {
                listener.onDestroy(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnCanceled() {
        for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
            TaskListener listener = listeners[i];
            try {
                listener.onCanceled(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnFailed() {
        for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
            TaskListener listener = listeners[i];
            try {
                listener.onFailed(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }

    private void callOnSucceed() {
        for (int i = listeners.length - 1; i >= 0; i--) { // in reverse order
            TaskListener listener = listeners[i];
            try {
                listener.onSucceed(this);
            } catch (Throwable throwable) {
                uncaughtListenerException(listener, throwable);
            }
        }
    }


    //////////////////////////////////////////////////////////////////
    // todo code below is useless and just a test
    //////////////////////////////////////////////////////////////////

    protected abstract void insertToQueue();

    protected abstract void removeFromQueue();

    protected abstract boolean isOwnerInterrupted();

    private final ExecutorService executorService = null;
    private volatile Future<Throwable> taskFuture = null;

    public void ___start() {
        synchronized (lock()) {
            if (isOwnerInterrupted()) {
                interrupted = true;
                state = State.CANCELED;
                throwable = null;
            } else {
                interrupted = false;
                state = State.CREATED;
                throwable = null;
                insertToQueue();
            }

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    if (isInterrupted()) {
                        callOnCreate();
                        callOnCanceled();
                        callOnDestroy();

                        // notify join object
                        synchronized (joinObject) {
                            joinObject.notifyAll();
                        }
                    } else {
                        callOnCreate();
                        callOnQueueInsert();

                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                if (isInterrupted()) {
                                    callOnCanceled();
                                    callOnQueueRemove();
                                    callOnDestroy();

                                    // notify join object
                                    synchronized (joinObject) {
                                        joinObject.notifyAll();
                                    }
                                } else {
                                    executeTask();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public void ___interrupt() {
        synchronized (lock()) {
            interrupted = true;

            switch (state) {
                case CREATED:
                    state = State.CANCELED;
                    throwable = null;
                    removeFromQueue();
                    break;
                case STARTED:
                    taskFuture.cancel(true);
                    break;
            }
        }
    }

    private void executeTask() {
        synchronized (lock()) {
            state = State.STARTED;
            throwable = null;
        }

        callOnStart();

        synchronized (lock()) {
            taskFuture = executorService.submit(new Callable<Throwable>() {
                @Override
                public Throwable call() throws Exception {
                    try {
                        task.run(env);
                        return null;
                    } catch (Throwable throwable) {
                        return throwable;
                    }
                }
            });
        }

        Throwable t = null;
        try {
            t = taskFuture.get();
        } catch (InterruptedException e) {
            t = e;
        } catch (ExecutionException e) {
            t = e.getCause();
        }

        synchronized (lock()) {
            if (t == null) {
                state = State.SUCCEED;
                throwable = null;
            } else {
                state = State.FAILED;
                throwable = t;
            }
            removeFromQueue();
        }

        callOnFinish();
        if (t == null) {
            callOnSucceed();
        } else {
            callOnFailed();
        }
        callOnQueueRemove();
        callOnDestroy();

        // notify join object
        synchronized (joinObject) {
            joinObject.notifyAll();
        }
    }

}
