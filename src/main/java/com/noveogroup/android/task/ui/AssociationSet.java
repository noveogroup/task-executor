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

import java.util.*;

/**
 * An association set.
 *
 * @param <V> a type of the values.
 */
// todo review & move into handler class
class AssociationSet<V> {

    private final Set<V> values = new HashSet<V>();
    private final Map<Object, Set<V>> associations = new HashMap<Object, Set<V>>();

    /**
     * Adds a value to the set.
     *
     * @param value the value.
     */
    public void add(V value) {
        values.add(value);
    }

    /**
     * Removes a value from the set.
     *
     * @param value the value.
     * @return true if the set contains the value and it is successfully removed.
     */
    public boolean remove(V value) {
        for (Iterator<Object> i = associations.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            Set<V> set = associations.get(key);
            set.remove(value);
            if (set.size() <= 0) {
                i.remove();
            }
        }

        return values.remove(value);
    }

    /**
     * Associates a value with keys.
     *
     * @param value the value.
     * @param keys  the keys.
     */
    public void associate(V value, Object... keys) {
        for (Object key : keys) {
            Set<V> set = associations.get(key);
            if (set == null) {
                set = new HashSet<V>();
                associations.put(key, set);
            }
            set.add(value);
        }
    }

    /**
     * Returns a set of associated values from the set.
     *
     * @param key the key to find values associated with.
     * @param <K> a type of the key.
     * @return the set of values.
     */
    public <K> Set<V> getAssociated(K key) {
        Set<V> set = associations.get(key);
        return set == null ? new HashSet<V>() : new HashSet<V>(set);
    }

}
