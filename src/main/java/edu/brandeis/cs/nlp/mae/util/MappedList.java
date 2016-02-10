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

public class MappedList<K,V> implements MappedCollection<K, V> {

    private TreeMap<K,List<V>> map;

    public MappedList(){
        map = new TreeMap<>();
    }

    public MappedList(HashMap<K, Collection<V>> map) {
        for (K key : map.keySet()) {
            map.put(key, new ArrayList<>(map.get(key)));
        }

    }

    @Override
    public boolean isSizeOne() {
        Set<K> keys  = map.keySet();
        return keys.size() == 1 && map.get(keys.iterator().next()).size() == 1;
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public ArrayList<K> keyList() {
        return new ArrayList<>(keySet());
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
        if (map.containsKey(key)) {
            getAsList(key).add(value);
        } else {
            ArrayList<V> newlist = new ArrayList<>();
            newlist.add(value);
            map.put(key, newlist);
        }
    }

    @Override
    public void putCollection(K key, Collection<V> collection) {
        if (map.containsKey(key)) {
            get(key).addAll(collection);
        } else {
            map.put(key, new ArrayList<>(collection));
        }

    }

    @Override
    public void merge(MappedCollection<K, V> newHash) {
        for (K key : newHash.keySet()) {
            putCollection(key, newHash.get(key));
        }

    }

    @Override
    public Collection<V> get(K key) {
        try {
            return map.get(key);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public ArrayList<V> getAsList(K key) {
        return new ArrayList<>(get(key));
    }

    @Override
    public Collection<V> remove(K key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return(map.size());
    }

    @Override
    public void clear() {
        map = new TreeMap<>();

    }

    @Override
    public boolean containsKey(K key) {
        return(map.containsKey(key));
    }

}

