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
 * Parent class for DTD elements (generally referred to as "tags")
 * 
 * @author Amber Stubbs
 */

import java.util.*;

class Elem extends Object{

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