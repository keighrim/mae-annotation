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

package edu.brandeis.cs.nlp.mae.model;


/**
 * Parent class for DTD elements (generally referred to as "tags")
 * @author Amber Stubbs, Keigh Rim
 *
 */

import edu.brandeis.cs.nlp.mae.model.Attrib;

import java.util.ArrayList;

public class Elem {

    Elem(){
        setName("no name");
        attributes=new ArrayList<Attrib>();
    }

    public String getName(){
        return name;
    }

    public void setName(String t){
        name=t;
    }

    public String toString(){
        return("name " + getName());
    }

    public void addAttribute(Attrib a){
        attributes.add(a);
    }

    public boolean removeAttribute(String attName) {
        for (Attrib a : attributes) {
            if (a.getName().equals(attName)) {
                attributes.remove(a);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Attrib> getAttributes(){
        return attributes;
    }

    public Attrib getAttribute(String name){
        for(int i=0;i<attributes.size();i++){
            if ((attributes.get(i).getName()).equalsIgnoreCase(name)){
                return attributes.get(i);
            }
        }
        return null;
    }

    boolean hasAttribute(String name){
        for(int i=0;i<attributes.size();i++){
            if ((attributes.get(i).getName()).equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }

    void printInfo(){
        System.out.println(name);
        System.out.println("Attributes:");
        for(int i=0;i<attributes.size();i++){
            attributes.get(i).printInfo();
            System.out.println("\n");
        }
    }

    private ArrayList<Attrib> attributes;
    private String name;
}