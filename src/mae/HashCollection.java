/*
 * This file is part of MAE - Multi-purpose Annotation Environment
 * 
 * Copyright Amber Stubbs (astubbs@cs.brandeis.edu)
 * Department of Computer Science, Brandeis University
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package mae;

/**
 * This is an implementation of a Hashtable that 
 * stores more than one value per key.  This is done by 
 * having every key associated with an ArrayList, and every
 * new value being stored in the array is added to the end of
 * the list (unless the list already contains that value)
 * 
 */

import java.util.*;

class HashCollection<K,V>{

	private Hashtable<K,ArrayList<V>> hc;

	HashCollection(){
		hc = new Hashtable<K,ArrayList<V>>();
	}

	HashCollection(Hashtable<K,ArrayList<V>> ht){
		hc=ht;
	}

	/**
	 * Add an entity to a key's arrayList
	 *
	 * @param key Hashtable key
	 * @param value value being added to key's array
	 */
	void putEnt (K key, V value)
	{
		ArrayList<V> existing = getList(key);
		if ( existing == null ){
			ArrayList<V> newlist = new ArrayList<V>();
			newlist.add(value);
			hc.put(key, newlist);
		}
		else {
			//just add to tail end of existing ArrayList
			//but only if it's not already there
			if(!existing.contains(value)){
				existing.add(value);
			}
		}
	}

	void putAllEnt (K key, V value)
	{
		ArrayList<V> existing = getList(key);
		if ( existing == null ){
			ArrayList<V> newlist = new ArrayList<V>();
			newlist.add(value);
			hc.put(key, newlist);
		}
		else {
			//just add to tail end of existing ArrayList
			//even if the value is already there
			existing.add(value);
		}
	}

	Hashtable<V,String> getValueHash(){
		Hashtable<V,String> values = new Hashtable<V,String>();
		Iterator<ArrayList<V>> it = hc.values().iterator();
		while(it.hasNext()){
			ArrayList<V> a1 = it.next();
			if(a1!=null){
				for (int j=0;j<a1.size();j++){
					if(a1.get(j)!=null){
						values.put(a1.get(j),"");
					}
				}
			}
		}
		return(values);
	}

	void printKeys(){
		for (Enumeration<K> e = hc.keys() ; e.hasMoreElements() ;) {
			System.out.println(e.nextElement());
		}
	}

	ArrayList<K> getKeyList(){
		ArrayList<K> keys = new ArrayList<K>();
		for (Enumeration<K> e = hc.keys() ; e.hasMoreElements() ;) {
			keys.add(e.nextElement());
		}
		return(keys);
	}

	void printHash(){
		for (Enumeration<K> e = hc.keys() ; e.hasMoreElements() ;) {
			K ent = e.nextElement();
			System.out.println((String)ent + ":");
			ArrayList<V> list = getList(ent);
			for (int i=0;i<list.size();i++){
				System.out.println("\t" + list.get(i).toString());
			}
		}

	}

	void putAll(HashCollection<K,V> h){
		for (Enumeration<K> e = hc.keys() ; e.hasMoreElements() ;) {
			K ent = e.nextElement();
			if (hc.containsKey(ent)){
				ArrayList<V> vals = h.getList(ent);
				if(vals !=null){
					for(int i=0;i<vals.size();i++){
						putEnt(ent,vals.get(i));
					}
				}
			}
			else{
				ArrayList<V> vals = h.getList(ent);
				if(vals !=null){
					for(int i=0;i<vals.size();i++){
						putEnt(ent,vals.get(i));
					}
				}
			}

		}
	}

	void putList(K key, ArrayList<V> list){
		for(int i=0;i<list.size();i++){
			putEnt(key,list.get(i));
		}
	}

	ArrayList<V> getList(K key){
		ArrayList<V> k = hc.get(key);
		if (k==null){
			return(null);
		}
		else{
			return(k);
		}
	}

	int size(){
		return(hc.size());
	}

	void remove(K key){
		hc.remove(key);
	}

	boolean containsKey(K key){
		return(hc.containsKey(key));
	}

	ArrayList<V> get(K key){
		return(hc.get(key));
	}

	Enumeration<K> keys(){
		return(hc.keys());
	}


}
