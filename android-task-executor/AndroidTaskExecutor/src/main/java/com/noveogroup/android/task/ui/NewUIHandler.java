/*
 * Copyright (c) 2014 Noveo Group
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * {@link NewUIHandler} provides you an interface to process {@link Runnable}
 * callbacks using usual Android {@link Handler}.
 * <p/>
 * Scheduling callbacks is accomplished with the {@link #post(Runnable)},
 * {@link #post(long, Runnable)} and {@link #sync(Runnable)} methods.
 * <p/>
 * Scheduled callbacks may be waited using {@link #join()} and
 * {@link #join(Runnable)} methods or removed using {@link #remove()} or
 * {@link #remove(Runnable)} methods.
 * <p/>
 * It is a good practice to call {@link #remove()} when {@link NewUIHandler}
 * is no longer needed.
 */
public class NewUIHandler {

    private static class AssociationMap<K, V> {

        private final Object lockObject;
        private final Map<K, Set<V>> values = new HashMap<K, Set<V>>();
        private final Map<V, Set<String>> associations = new HashMap<V, Set<String>>();

        public AssociationMap(Object lockObject) {
            this.lockObject = lockObject;
        }

        public void add(K key, V value, Collection<String> tags) {
            synchronized (lockObject) {
                Set<V> set = values.get(key);
                if (set == null) {
                    set = new HashSet<V>();
                }
                set.add(value);

                associations.put(value, new HashSet<String>(tags));
            }
        }

        public void remove(K key, V value) {
            synchronized (lockObject) {
                Set<V> set = values.get(key);
                if (set != null) {
                    set.remove(value);
                    if (set.isEmpty()) {
                        values.remove(key);
                    }
                }

                associations.remove(value);
            }
        }

        public Set<V> getAssociated(K key, Collection<String> tags) {
            synchronized (lockObject) {
                Set<V> associated = new HashSet<V>();

                Set<V> set = values.get(key);
                if (set != null) {
                    for (V value : set) {
                        if (associations.get(value).containsAll(tags)) {
                            associated.add(value);
                        }
                    }
                }

                return Collections.unmodifiableSet(associated);
            }
        }

        public Set<V> getAssociated(Collection<String> tags) {
            synchronized (lockObject) {
                Set<V> associated = new HashSet<V>();
                for (K key : values.keySet()) {
                    associated.addAll(getAssociated(key, tags));
                }
                return Collections.unmodifiableSet(associated);
            }
        }

        public Set<V> removeAssociated(K key, Collection<String> tags) {
            synchronized (lockObject) {
                Set<V> associated = getAssociated(key, tags);
                for (V value : associated) {
                    remove(key, value);
                }
                return associated;
            }
        }

        public Set<V> removeAssociated(Collection<String> tags) {
            synchronized (lockObject) {
                Set<V> associated = new HashSet<V>();
                for (K key : values.keySet()) {
                    associated.addAll(removeAssociated(key, tags));
                }
                return Collections.unmodifiableSet(associated);
            }
        }

    }

    private static class WaitCallback implements Runnable {

        private final Object waitObject = new Object();
        private boolean finished = false;

        protected void runCallback() {
        }

        @Override
        public final void run() {
            try {
                runCallback();
            } finally {
                release();
            }
        }

        public final void join() throws InterruptedException {
            synchronized (waitObject) {
                while (!finished) {
                    waitObject.wait();
                }
            }
        }

        public final void release() {
            synchronized (waitObject) {
                finished = true;
                waitObject.notifyAll();
            }
        }

    }

    private final Object lock;
    private final Handler handler;
    private final AssociationMap<Runnable, WaitCallback> associationMap;

    private final NewUIHandler root;
    private final Set<String> tags;
    private final WeakHashMap<Set<String>, NewUIHandler> subCache;

    /**
     * Default constructor associates this handler with the queue for
     * the current thread. If there isn't one, this handler won't be able
     * to receive messages.
     */
    public NewUIHandler() {
        this(new Handler());
    }

    /**
     * Constructor uses the provided queue and its handler instead of
     * the default one.
     *
     * @param looper the custom queue.
     */
    public NewUIHandler(Looper looper) {
        this(new Handler(looper));
    }

    /**
     * Constructor uses main looper of the provided context to initialize
     * the handler.
     *
     * @param context the context.
     */
    public NewUIHandler(Context context) {
        this(new Handler(context.getMainLooper()));
    }

    /**
     * Constructor uses the specified handler to delegate callbacks to.
     *
     * @param handler the delegate handler.
     */
    public NewUIHandler(Handler handler) {
        this.lock = new Object();
        this.handler = handler;
        this.associationMap = new AssociationMap<Runnable, WaitCallback>(lock);

        this.root = this;
        this.tags = Collections.emptySet();
        this.subCache = new WeakHashMap<Set<String>, NewUIHandler>();
    }

    private NewUIHandler(NewUIHandler root, Set<String> tags) {
        this.lock = root.lock;
        this.handler = root.handler;
        this.associationMap = root.associationMap;

        this.tags = tags;
        this.root = root;
        this.subCache = root.subCache;
    }

    private static Set<String> union(Collection<String> set1, Collection<String> set2) {
        HashSet<String> set = new HashSet<String>(set1.size() + set2.size());
        set.addAll(set1);
        set.addAll(set2);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns sub-handler associated with the specified set of tags.
     * <p/>
     * The callbacks of the sub-handler are owned by parent handler too.
     *
     * @param tags the set of tags.
     * @return the sub-handler.
     */
    public NewUIHandler sub(String... tags) {
        return sub(Arrays.asList(tags));
    }

    /**
     * Returns sub-handler associated with the specified set of tags.
     * <p/>
     * The callbacks of the sub-handler are owned by parent handler too.
     *
     * @param tags the set of tags.
     * @return the sub-handler.
     */
    public NewUIHandler sub(Collection<String> tags) {
        Set<String> tagSet = union(this.tags, tags);

        synchronized (lock) {
            NewUIHandler uiHandler = subCache.get(tagSet);
            if (uiHandler == null) {
                uiHandler = new NewUIHandler(root, tagSet);
                subCache.put(tagSet, uiHandler);
            }
            return uiHandler;
        }
    }

    /**
     * Returns a root handler.
     *
     * @return the root handler.
     */
    public NewUIHandler root() {
        return root;
    }

    /**
     * Returns a set of tags associated with this handler.
     *
     * @return the set of tags.
     */
    public Set<String> tags() {
        return tags;
    }

    private void checkJoinAbility() {
        if (Thread.currentThread() == handler.getLooper().getThread()) {
            throw new RuntimeException("current thread is a looper thread and cannot wait itself");
        }
    }

    private WaitCallback createWaitCallback(final Runnable callback) {
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        return new WaitCallback() {
            @Override
            protected void runCallback() {
                try {
                    callback.run();
                } finally {
                    associationMap.remove(callback, this);
                }
            }
        };
    }

    private WaitCallback postCallback(Runnable callback) {
        WaitCallback waitCallback = createWaitCallback(callback);

        synchronized (lock) {
            if (handler.post(waitCallback)) {
                associationMap.add(callback, waitCallback, tags);
                return waitCallback;
            } else {
                throw new RuntimeException("cannot post callback to the handler");
            }
        }
    }

    private WaitCallback postCallback(long delay, Runnable callback) {
        WaitCallback waitCallback = createWaitCallback(callback);

        synchronized (lock) {
            if (handler.postDelayed(waitCallback, delay)) {
                associationMap.add(callback, waitCallback, tags);
                return waitCallback;
            } else {
                throw new RuntimeException("cannot post callback to the handler");
            }
        }
    }

    /**
     * Causes the callback to be added to the queue.
     *
     * @param callback the callback that will be executed.
     */
    public void post(Runnable callback) {
        postCallback(callback);
    }

    /**
     * Causes the callback to be added to the queue.
     *
     * @param callback the callback that will be executed.
     * @param delay    the delay (in milliseconds) until the callback will be
     *                 executed.
     */
    public void post(long delay, Runnable callback) {
        postCallback(delay, callback);
    }


    /**
     * Adds the callback to the queue using {@link #post(Runnable)} and
     * wait for it after that.
     *
     * @param callback the callback that will be executed.
     */
    public void sync(Runnable callback) throws InterruptedException {
        checkJoinAbility();
        postCallback(callback).join();
    }

    /**
     * Joins the specified callback. If the callback will be removed before
     * finish this method successfully ends.
     *
     * @param callback the callback to join to.
     * @throws InterruptedException if any thread interrupted the current one.
     */
    public void join(Runnable callback) throws InterruptedException {
        checkJoinAbility();
        for (WaitCallback waitCallback : associationMap.getAssociated(callback, tags)) {
            waitCallback.join();
        }
    }

    /**
     * Joins all callbacks of the handler. If the callbacks will be removed
     * before finish this method successfully ends.
     *
     * @throws InterruptedException if any thread interrupted the current one.
     */
    public void join() throws InterruptedException {
        checkJoinAbility();
        for (WaitCallback waitCallback : associationMap.getAssociated(tags)) {
            waitCallback.join();
        }
    }

    /**
     * Removes any pending posts of the specified callback that are in
     * the message queue. All waiting threads joining to this callback
     * will be notified and resumed.
     *
     * @param callback the callback to remove.
     */
    public void remove(Runnable callback) {
        for (WaitCallback waitCallback : associationMap.removeAssociated(callback, tags)) {
            waitCallback.release();
        }
    }

    /**
     * Removes any callbacks from the queue. All waiting threads joining
     * to callbacks of this handler will be notified and resumed.
     */
    public void remove() {
        for (WaitCallback waitCallback : associationMap.removeAssociated(tags)) {
            waitCallback.release();
        }
    }

}
