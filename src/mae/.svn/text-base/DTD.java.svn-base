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
 * 
 * Provides a description of the annotation task information 
 * stored in a DTD.  The DTD describes the annotation 
 * task, specifically the tags and attributes.
 * 
 * @author Amber Stubbs
 *
 */

import java.util.*;

class DTD extends Object{

DTD(){
    elements=new ArrayList<Elem>();
    name="XML";
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

public void addElem(Elem t){
    elements.add(t);
}

public Elem getElem(String name){
    for(int i=0;i<elements.size();i++){
        Elem e = elements.get(i);
        if (e.getName().equalsIgnoreCase(name)){
            return e;
        }
    }
    return null;
}

public boolean hasElem(String name){
    for(int i=0;i<elements.size();i++){
        Elem e = elements.get(i);
        if (e.getName().equalsIgnoreCase(name)){
            return true;
        }
    }
    return false;
}

public ArrayList<Elem> getElements(){
    return elements;
}

public ArrayList<String> getElementIDs(){
    ArrayList<String> ids = new ArrayList<String>();
    
    for (int i=0;i<elements.size();i++){
        Elem e = elements.get(i);
        AttID a = (AttID)e.getAttribute("id");
        ids.add(a.getPrefix());
    }
    return ids;
}

public ArrayList<Elem> getNCElements(){
    ArrayList<Elem> NCElems = new ArrayList<Elem>();
    //returns a list of non-consuming extent tags
    for(int i=0;i<elements.size();i++){
        Elem e = elements.get(i);
        if(e instanceof ElemExtent){
            ElemExtent ex = (ElemExtent)e;
            Attrib start = ex.getAttribute("start");
            if(!(start.getRequired())){
                NCElems.add(e);
            }
        }
    }
    return NCElems;
}

public void printInfo(){
    System.out.println(name);
    System.out.println("Elements:");
    for(int i=0;i<elements.size();i++){
        System.out.println(" Element " + i);
        elements.get(i).printInfo();
        System.out.println("\n");
    }
}

private ArrayList<Elem> elements;
private String name;

}