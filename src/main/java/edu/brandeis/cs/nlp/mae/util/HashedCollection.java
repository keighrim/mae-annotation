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

public abstract class HashedCollection<K,V>{

    private HashMap<K,Collection<V>> hash;

    public HashedCollection(){
        hash = new HashMap<>();
    }

    public HashedCollection(HashMap<K,Collection<V>> hash){
        this.hash = hash;
    }

    /**
     * returns true if their's only one key and one item associated with that key
     */
    public boolean isSizeOne() {
        Set<K> keys  = hash.keySet();
        return keys.size() == 1 && hash.get(keys.iterator().next()).size() == 1;
    }

    public Set<K> keySet(){
        return hash.keySet();
    }

    public ArrayList<K> keyList(){
        return new ArrayList<>(keySet());
    }

    public abstract void putItem(K key, V value);

    public void putCollection(K key, Collection<V> collection){
        get(key).addAll(collection);
    }

    /**
     * Add all key-value pairs of a new HashCollection to this object
     * @param newHash - target HashCollection
     */
    public void merge(HashedCollection<K,V> newHash){
        for (K key : newHash.keySet()) {
            putCollection(key, newHash.get(key));
        }
    }

    public Collection<V> get(K key) {
        try {
            return hash.get(key);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public ArrayList<V> getAsList(K key){
        return new ArrayList<>(get(key));
    }

    public Collection<V> remove(K key){
        return hash.remove(key);
    }

    public int size(){
        return(hash.size());
    }

    public void clear() {
        hash = new HashMap<>();
    }

    public boolean containsKey(K key){
        return(hash.containsKey(key));
    }

}

