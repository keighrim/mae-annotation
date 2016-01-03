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

import java.util.*;

public class MappedList<K,V> extends MappedCollection<K, V> {

    // TODO: 1/3/2016 check later, if this is the right way to inherit 
    private TreeMap<K,LinkedList<V>> hash;

    public MappedList(){
        hash = new TreeMap<>();
    }

    /**
     * Associate yet another value with a key in a Hashtable that allows duplicates.
     * TODO 151214 figure out why we need to allow duplicates
     * Also use to put the first key/value.
     * Add an entity to a key's arrayList
     *
     * @param key Hashtable key
     * @param value value being added to key's array
     */
    public void putItem (K key, V value) {
        if (super.hash.containsKey(key)) {
            getAsList(key).add(value);
        } else {
            ArrayList<V> newlist = new ArrayList<>();
            newlist.add(value);
            super.hash.put(key, newlist);
        }
    }

}
