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

public abstract class AbstractTaskHandler<T extends Task, E extends TaskEnvironment> implements TaskHandler<T, E> {

    private final TaskSet<E> owner;
    private final T task;
    private final Pack args;

    protected AbstractTaskHandler(TaskSet<E> owner, T task, Pack args) {
        this.owner = owner;
        this.task = task;
        this.args = args.lock() == owner.lock() ? args : new Pack(owner.lock(), args);
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Throwable getThrowable() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInterrupted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void interrupt() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void join() throws InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean join(long timeout) throws InterruptedException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

//    private volatile State status;
//    private volatile Throwable throwable;
//    private volatile boolean interrupted;
//    private final TaskListener[] listeners;
//    private final Object joinObject = new Object();
//
//    public AbstractTaskHandler(Object lock, TaskSet<E> owner, T task, Collection<Object> tags, TaskListener... taskListeners) {
//        this.lock = lock;
//        this.owner = owner;
//        this.task = task;
//        this.tags = Collections.unmodifiableSet(new HashSet<Object>(tags));
//        this.status = State.CREATED;
//        this.throwable = null;
//        this.interrupted = false;
//        this.listeners = new TaskListener[taskListeners.length];
//        System.arraycopy(taskListeners, 0, this.listeners, 0, taskListeners.length);
//    }
//
//    @Override
//    public State getState() {
//        synchronized (lock) {
//            return status;
//        }
//    }
//
//    @Override
//    public Throwable getThrowable() {
//        synchronized (lock) {
//            return throwable;
//        }
//    }
//
//    @Override
//    public boolean isInterrupted() {
//        synchronized (lock) {
//            return interrupted;
//        }
//    }
//
//    @Override
//    public void interrupt() {
//        synchronized (lock) {
//            interrupted = true;
//        }
//        doInterrupt();
//    }
//
//    protected void doInterrupt() {
//        // todo not implemented anywhere
//    }
//
//    @Override
//    public void join() throws InterruptedException {
//        join(0);
//    }
//
//    @Override
//    public void join(long timeout) throws InterruptedException {
//        if (timeout < 0) {
//            throw new IllegalArgumentException();
//        }
//
//        synchronized (joinObject) {
//            while (!getState().isDestroyed()) {
//                if (timeout == 0) {
//                    joinObject.wait();
//                } else {
//                    long time = SystemClock.uptimeMillis();
//                    joinObject.wait(timeout);
//                    timeout -= SystemClock.uptimeMillis() - time;
//
//                    if (timeout <= 0) {
//                        break;
//                    }
//                }
//            }
//        }
//    }
//
//    private void uncaughtListenerException(TaskListener listener, Throwable throwable) {
//        Log.e(TaskExecutor.TAG, "listener " + listener + " failed", throwable);
//        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), throwable);
//    }
//
//    private void callOnCreate() {
//        for (TaskListener listener : listeners) {
//            try {
//                listener.onCreate(this);
//            } catch (Throwable throwable) {
//                uncaughtListenerException(listener, throwable);
//            }
//        }
//    }
//
//    private void callOnCancel() {
//        for (int i = listeners.length - 1; i >= 0; i--) {
//            TaskListener listener = listeners[i];
//            try {
//                listener.onCancel(this);
//            } catch (Throwable throwable) {
//                uncaughtListenerException(listener, throwable);
//            }
//        }
//    }
//
//    private void callOnStart() {
//        for (TaskListener listener : listeners) {
//            try {
//                listener.onStart(this);
//            } catch (Throwable throwable) {
//                uncaughtListenerException(listener, throwable);
//            }
//        }
//    }
//
//    private void callOnFinish() {
//        for (int i = listeners.length - 1; i >= 0; i--) {
//            TaskListener listener = listeners[i];
//            try {
//                listener.onFinish(this);
//            } catch (Throwable throwable) {
//                uncaughtListenerException(listener, throwable);
//            }
//        }
//    }
//
//    private void callOnSucceed() {
//        for (int i = listeners.length - 1; i >= 0; i--) {
//            TaskListener listener = listeners[i];
//            try {
//                listener.onSucceed(this);
//            } catch (Throwable throwable) {
//                uncaughtListenerException(listener, throwable);
//            }
//        }
//    }
//
//    private void callOnFailed() {
//        for (int i = listeners.length - 1; i >= 0; i--) {
//            TaskListener listener = listeners[i];
//            try {
//                listener.onFailed(this);
//            } catch (Throwable throwable) {
//                uncaughtListenerException(listener, throwable);
//            }
//        }
//    }
//
//    private void callOnDestroy() {
//        for (int i = listeners.length - 1; i >= 0; i--) {
//            TaskListener listener = listeners[i];
//            try {
//                listener.onDestroy(this);
//            } catch (Throwable throwable) {
//                uncaughtListenerException(listener, throwable);
//            }
//        }
//    }
//
//    public void onQueueInsert() {
//        callOnCreate();
//    }
//
//    public void onQueueRemove() {
//        callOnDestroy();
//    }
//
//    public void onCancel() {
//        synchronized (lock) {
//            status = State.CANCELED;
//        }
//        callOnCancel();
//    }
//
//    public void onExecute() {
//        synchronized (lock) {
//            status = State.STARTED;
//        }
//
//        try {
//            try {
//                callOnStart();
//                task().run(this, env);
//            } finally {
//                callOnFinish();
//            }
//
//            synchronized (lock) {
//                status = State.SUCCEED;
//            }
//            callOnSucceed();
//        } catch (Throwable t) {
//            synchronized (lock) {
//                status = State.FAILED;
//                throwable = t;
//            }
//            callOnFailed();
//        }
//    }
//
//    public void notifyJoinObject() {
//        synchronized (joinObject) {
//            joinObject.notifyAll();
//        }
//    }

}
