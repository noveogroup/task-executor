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

/**
 * A {@link Pack} is a data structure consisting of a set of arguments.
 * Each argument has a unique key that corresponds to a value.
 * <p/>
 * A {@link Pack} provides helper methods to iterate through all of
 * the arguments contained, as well as various methods to access and update
 * the arguments.
 * <p/>
 * Any access to the pack is synchronized using a special object returning
 * by {@link Pack#lock()} method. This object can be used outside of pack
 * to synchronize complex and dependent sequences of accesses/updates.
 */
public final class Pack implements Iterable<String> {

    private final Object lock;
    private final HashMap<String, Object> map;

    /**
     * Constructs a new empty pack.
     */
    public Pack() {
        this(new Object());
    }

    /**
     * Constructs a new empty pack.
     *
     * @param lock synchronization object.
     */
    public Pack(Object lock) {
        this.lock = lock;
        this.map = new HashMap<String, Object>();
    }

    /**
     * Constructs a new packs containing the arguments from
     * the specified one.
     *
     * @param pack the pack of arguments to add.
     */
    public Pack(Pack pack) {
        this(pack.lock());
        putAll(pack);
    }

    /**
     * Constructs a new packs containing the arguments from
     * the specified one.
     *
     * @param lock synchronization object.
     * @param pack the pack of arguments to add.
     */
    public Pack(Object lock, Pack pack) {
        this(lock);
        putAll(pack);
    }

    public Pack copy() {
        return new Pack(this);
    }

    public Pack copy(Object lock) {
        return new Pack(lock, this);
    }

    /**
     * Returns synchronization object of this pack.
     *
     * @return the synchronization object.
     */
    public Object lock() {
        return lock;
    }

    /**
     * Returns the number of arguments in this pack.
     *
     * @return the number of arguments in this pack.
     */
    public int size() {
        synchronized (lock()) {
            return map.size();
        }
    }

    /**
     * Returns whether this pack is empty.
     *
     * @return {@code true} if this pack has no arguments, {@code false} otherwise.
     * @see #size()
     */
    public boolean isEmpty() {
        synchronized (lock()) {
            return map.isEmpty();
        }
    }

    /**
     * Returns an iterator over a set of keys of arguments contained in
     * this pack. The iterator supports removing.
     *
     * @return an iterator.
     */
    @Override
    public Iterator<String> iterator() {
        synchronized (lock()) {
            // copy key set and create delegate iterator
            HashSet<String> copyKeySet = new HashSet<String>(map.keySet());
            final Iterator<String> delegate = copyKeySet.iterator();

            // return a thread-safe iterator
            return new Iterator<String>() {
                private final Object iteratorLock = new Object();
                private String currentKey = null;

                @Override
                public boolean hasNext() {
                    synchronized (iteratorLock) {
                        return delegate.hasNext();
                    }
                }

                @Override
                public String next() {
                    synchronized (iteratorLock) {
                        return currentKey = delegate.next();
                    }
                }

                @Override
                public void remove() {
                    synchronized (iteratorLock) {
                        // implicitly check position of cursor
                        delegate.remove();
                        // so currentKey is valid
                        Pack.this.remove(currentKey);
                    }
                }
            };
        }
    }

    /**
     * Returns a set of the keys of arguments contained in this pack.
     * The set supports modifications except adding.
     *
     * @return a set of the keys.
     */
    public Set<String> keySet() {
        return new AbstractSet<String>() {
            @Override
            public Iterator<String> iterator() {
                return Pack.this.iterator();
            }

            @Override
            public int size() {
                return Pack.this.size();
            }
        };
    }

    /**
     * Returns whether this pack contains an argument corresponding
     * to the specified key.
     *
     * @param key the key.
     * @return {@code true} if this pack contains the argument corresponding to
     * the specified key otherwise {@code false}.
     */
    public boolean containsKey(String key) {
        synchronized (lock()) {
            return map.containsKey(key);
        }
    }

    /**
     * Returns the value of an argument corresponding to the specified key.
     *
     * @param key the key.
     * @param <T> desired type of the value.
     * @return the value of the argument or {@code null} if no value
     * corresponding to this key was found.
     * @see #get(String, Object)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        synchronized (lock()) {
            return (T) map.get(key);
        }
    }

    /**
     * Returns the value of an argument corresponding to the specified key.
     *
     * @param key          the key.
     * @param defaultValue the default value.
     * @param <T>          desired type of the value.
     * @return the value of the argument or provided default if no value
     * corresponding to this key was found.
     * @see #get(String)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        synchronized (lock()) {
            T value = (T) map.get(key);
            return value != null ? value : defaultValue;
        }
    }

    /**
     * Clears the pack. Removes all arguments and leaves pack empty.
     *
     * @return this pack object.
     * @see #isEmpty()
     * @see #size()
     */
    public Pack clear() {
        synchronized (lock()) {
            map.clear();
            return this;
        }
    }

    /**
     * Removes an argument corresponding the specified key.
     *
     * @param key the key.
     * @return this pack object.
     */
    public Pack remove(String key) {
        synchronized (lock()) {
            map.remove(key);
            return this;
        }
    }

    /**
     * Updates an argument corresponding to the specified key and sets its value to the specified one.
     *
     * @param key   the key.
     * @param value the value.
     * @param <T>   the type of the value.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #put(String, Object, Object)
     * @see #putIf(boolean, String, Object)
     * @see #putAll(Pack)
     */
    public <T> Pack put(String key, T value) {
        synchronized (lock()) {
            map.put(key, value);
            return this;
        }
    }

    /**
     * Updates an argument corresponding to the specified key and sets its value to the specified one
     * or, if the specified value is {@code null} - to the specified default one.
     *
     * @param key          the key.
     * @param value        the value.
     * @param defaultValue the default value to use when the usual one is {@code null}.
     * @param <T>          the type of the value.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #putIf(boolean, String, Object)
     * @see #putAll(Pack)
     */
    public <T> Pack put(String key, T value, T defaultValue) {
        synchronized (lock()) {
            if (value != null) {
                map.put(key, value);
            } else {
                map.put(key, defaultValue);
            }
            return this;
        }
    }

    /**
     * Updates an argument just like {@link #put(String, Object)} but do it if only the specified
     * condition is {@code true}.
     *
     * @param condition the condition.
     * @param key       the key.
     * @param value     the value.
     * @param <T>       the type of the value.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #put(String, Object, Object)
     * @see #putAll(Pack)
     */
    public <T> Pack putIf(boolean condition, String key, T value) {
        synchronized (lock()) {
            if (condition) {
                map.put(key, value);
            }
            return this;
        }
    }

    /**
     * Copies all the arguments in the specified pack to this pack.
     *
     * @param pack the pack to copy arguments from.
     * @return this pack object.
     * @see #put(String, Object)
     * @see #put(String, Object, Object)
     * @see #putIf(boolean, String, Object)
     */
    public Pack putAll(Pack pack) {
        synchronized (lock()) {
            synchronized (pack.lock()) {
                map.putAll(pack.map);
            }
            return this;
        }
    }

}
