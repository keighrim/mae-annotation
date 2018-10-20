/*
 * MAE - Multi-purpose Annotation Environment
 *
 * Copyright Keigh Rim (krim@brandeis.edu)
 * Department of Computer Science, Brandeis University
 * Original program by Amber Stubbs (astubbs@cs.brandeis.edu)
 *
 * MAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, @see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
 *
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.llc.mae.util;

/**
 * This is an implementation of a Hashtable that 
 * stores more than one value per key.  This is done by 
 * having every key associated with an ArrayList, and every
 * new value being stored in the array is added to the end of
 * the list (unless the list already contains that value)
 *
 * @author Amber Stubbs, Keigh Rim
 *
 *
 */


import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public interface MappedCollection<K,V>{

    boolean isSizeOne();

    Set<K> keySet();

    ArrayList<K> keyList();

    void putItem(K key, V value);

    void putCollection(K key, Collection<V> collection);

    void merge(MappedCollection<K,V> newHash);

    Collection<V> get(K key);

    ArrayList<V> getAsList(K key);

    Collection<V> remove(K key);

    int size();

    void clear();

    boolean containsKey(K key);

}

