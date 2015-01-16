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
 * @revised Keigh Rim
 *
 */

import java.util.*;

class DTD {

    private ArrayList<Elem> mElements;
    private String mName;
    private int mMaxArgs;

    DTD(){
        mElements =new ArrayList<Elem>();
        mName ="XML";
        mMaxArgs = 0;  // by default, set max # arguments to 2

    }

    // TODO polish these MaxArgs getter and setter
    public int getMaxArgs() {
        return mMaxArgs;
    }

    public void setMaxArgs(int i) {
        mMaxArgs = i;
    }

    public String getName(){
        return mName;
    }

    public void setName(String t){
        mName =t;
    }

    public String toString(){
        return("name " + getName());
    }

    public void addElem(Elem t){
        mElements.add(t);
    }

    public Elem getElem(String name){
        for (Elem e : mElements) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    public boolean hasElem(String name){
        for (Elem e : mElements) {
            if (e.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Elem> getElements(){
        return mElements;
    }

    /**
     * Returns a list of all the elements in the DTD
     * @return
     */
    public ArrayList<String> getElementIDs(){
        ArrayList<String> ids = new ArrayList<String>();

        for (Elem e : mElements) {
            AttID a = (AttID) e.getAttribute("id");
            ids.add(a.getPrefix());
        }
        return ids;
    }

    /**
     * Returns a list of non-consuming extent tags
     *
     * @return
     */
    public ArrayList<Elem> getNCElements(){
        ArrayList<Elem> NCElems = new ArrayList<Elem>();
        //returns a list of non-consuming extent tags
        for (Elem e : mElements) {
            if (e instanceof ElemExtent) {
                ElemExtent ex = (ElemExtent) e;
                if (!ex.getAttribute("spans").isRequired()) {
                    NCElems.add(e);
                }
            }
        }
        return NCElems;
    }

    public void printInfo(){
        System.out.println(mName);
        System.out.println("Elements:");
        for(int i=0;i< mElements.size();i++){
            System.out.println(" Element " + i);
            mElements.get(i).printInfo();
            System.out.println("\n");
        }
    }
}

// TODO seems done here
