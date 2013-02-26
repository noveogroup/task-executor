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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.*;

/**
 * {@link UIHandler} provides you an interface to process {@link Runnable}
 * callbacks using usual {@link Handler}.
 * <p/>
 * Scheduling callbacks is accomplished with the {@link #post(Runnable)} and
 * {@link #postDelayed(Runnable, long)} methods.
 * <p/>
 * Joining callbacks can cause a blocking. To ensure all threads will be resumed call {@link #removeCallbacks()}
 * when handler is no longer needed.
 */
public class UIHandler {

    private final Object lock = new Object();
    private final Handler handler;
    private final Set<Callback> set = new HashSet<Callback>();
    private final Map<Runnable, Set<Callback>> map = new HashMap<Runnable, Set<Callback>>();

    private class Callback implements Runnable {

        private final Runnable callback;
        private final Object waitObject = new Object();
        private volatile boolean finished = false;

        public Callback(Runnable callback) {
            this.callback = callback;
        }

        private void addCallback() {
            set.add(this);

            Set<Callback> callbacks = map.get(callback);
            if (callbacks == null) {
                callbacks = new HashSet<Callback>();
                map.put(callback, callbacks);
            }
            callbacks.add(this);
        }

        private void removeCallback() {
            set.remove(this);

            Set<Callback> callbacks = map.get(callback);
            if (callbacks != null) {
                callbacks.remove(this);
                if (callbacks.isEmpty()) {
                    map.remove(callback);
                }
            }
        }

        public boolean post() {
            synchronized (lock) {
                if (!handler.post(this)) {
                    return false;
                }
                addCallback();
                return true;
            }
        }

        public boolean postDelayed(long delay) {
            synchronized (lock) {
                if (!handler.postDelayed(this, delay)) {
                    return false;
                }
                addCallback();
                return true;
            }
        }

        @Override
        public final void run() {
            try {
                if (callback != null) {
                    callback.run();
                }
            } finally {
                release();
            }
        }

        public void release() {
            synchronized (lock) {
                handler.removeCallbacks(this);
                removeCallback();

                synchronized (waitObject) {
                    finished = true;
                    waitObject.notifyAll();
                }
            }
        }

        public void join() throws InterruptedException {
            synchronized (waitObject) {
                while (!finished) {
                    waitObject.wait();
                }
            }
        }

    }

    /**
     * Default constructor associates this handler with the queue for
     * the current thread. If there isn't one, this handler won't be able
     * to receive messages.
     */
    public UIHandler() {
        this(new Handler());
    }

    /**
     * Uses main looper of the context to initialize the handler.
     */
    public UIHandler(Context context) {
        this(context.getMainLooper());
    }

    /**
     * Use the provided queue instead of the default one.
     *
     * @param looper the custom queue.
     */
    public UIHandler(Looper looper) {
        this(new Handler(looper));
    }

    /**
     * Use the specified handler to delegate callbacks to.
     *
     * @param handler the delegate.
     */
    public UIHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * Causes the callback to be added to the queue.
     *
     * @param callback the callback that will be executed.
     */
    public boolean post(Runnable callback) {
        return new Callback(callback).post();
    }

    /**
     * Causes the callback to be added to the queue.
     *
     * @param callback the callback that will be executed.
     * @param delay    the delay (in milliseconds) until the callback will be
     *                 executed.
     */
    public boolean postDelayed(Runnable callback, long delay) {
        return new Callback(callback).postDelayed(delay);
    }

    private void joinCallbacks(Set<Callback> callbacks) throws InterruptedException {
        if (Thread.currentThread() == handler.getLooper().getThread()) {
            throw new RuntimeException("current thread blocks the callback");
        }

        for (Callback callback : callbacks) {
            callback.join();
        }
    }

    private void removeCallbacks(Set<Callback> callbacks) {
        for (Callback callback : callbacks) {
            callback.release();
        }
    }

    private Set<Callback> getCallbacks(Runnable callback) {
        synchronized (lock) {
            Set<Callback> callbacks = map.get(callback);
            return callbacks == null ? Collections.<Callback>emptySet() : new HashSet<Callback>(callbacks);
        }
    }

    private Set<Callback> getCallbacks() {
        synchronized (lock) {
            return new HashSet<Callback>(set);
        }
    }

    /**
     * Joins the specified callback. If the callback will be removed before
     * finish this method successfully ends.
     *
     * @param callback the callback to join to.
     * @throws InterruptedException if any thread interrupted the current one.
     */
    public void joinCallbacks(Runnable callback) throws InterruptedException {
        joinCallbacks(getCallbacks(callback));
    }

    /**
     * Joins all callbacks of the handler. If the callbacks will be removed
     * before finish this method successfully ends.
     *
     * @throws InterruptedException if any thread interrupted the current one.
     */
    public void joinCallbacks() throws InterruptedException {
        joinCallbacks(getCallbacks());
    }

    /**
     * Removes any pending posts of the specified callback that are in
     * the message queue. All waiting threads joining to this callback
     * will be notified and resumed.
     *
     * @param callback the callback to remove.
     */
    public void removeCallbacks(Runnable callback) {
        removeCallbacks(getCallbacks(callback));
    }

    /**
     * Removes any callbacks from the queue. All waiting threads joining
     * to callbacks of this handler will be notified and resumed.
     */
    public void removeCallbacks() {
        removeCallbacks(getCallbacks());
    }

}
