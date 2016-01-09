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
 * For feedback, reporting bugs, use the project repo on github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>
 */

package edu.brandeis.cs.nlp.mae.util;

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

import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class MappedSet<K,V> extends MappedCollection<K, V> {

    private TreeMap<K,TreeSet<V>> hash;

    public MappedSet(){
        hash = new TreeMap<>();
    }

    /**
     * Associate yet another value with a key in a Hashtable that doesn't allows duplicates, but sorted
     * Also use to put the first key/value.
     * Add an entity to a key's arrayList
     *
     * @param key Hashtable key
     * @param value value being added to key's array
     */
    public void putItem (K key, V value) {
        if (super.containsKey(key)) {
            getAsList(key).add(value);
        } else {
            TreeSet<V> newtree = new TreeSet<>();
            newtree.add(value);
            hash.put(key, newtree);
        }
    }
}