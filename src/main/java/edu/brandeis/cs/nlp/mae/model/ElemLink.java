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


import edu.brandeis.cs.nlp.mae.MaeStrings;

import java.util.ArrayList;


/**
 * Extends Elem; used for describing link tags
 * @author Amber Stubbs, Keigh Rim
 *
 */


public class ElemLink extends Elem {

    private ArrayList<String> mArguments;
    private boolean mNary;


    ElemLink() {
        mArguments = new ArrayList<String>();
        mNary = false;
    }


    /**
     * Default constructor for legacy from-to link
     * @param name name of tag type
     * @param idString string indicating tag's ID
     */
    public ElemLink(String name, String idString) {
        setName(name);
        AttID id = new AttID("id", idString, true);
        addAttribute(id);

        // by default, add "from" and "to" arguments for legacy binary linking
        mArguments = new ArrayList<String>();
        mArguments.add("from");
        mArguments.add("to");
        mNary = false;
        for (String argName : mArguments) {
            addArgAtts(argName);
        }
    }

    /**
     * Retrieve the list of arguments
     * @return
     */
    public ArrayList<String> getArguments() {
        return mArguments;
    }

    public int getArgNum() {
        return mArguments.size();
    }

    /**
     * see if this tag is legacy binary or n-ary
     * @return true if it has arbitrary n-ary arguments
     */
    public boolean isNary() {
        return mNary;
    }

    /**
     * reset argument attributes, used when set this tag as a n-ary link tag
     */
    public void setNary() {
        removeAttribute("fromID");
        removeAttribute("fromText");
        removeAttribute("toID");
        removeAttribute("toText");
        mArguments.clear();
        mNary = true;
    }

    public void addArgement(String argName) {
        // if the link tag is binary one (from-to), remove from-to attribs first
        if (!isNary()) {
            setNary();
        }
        mArguments.add(argName);
        addArgAtts(argName);
    }
    
    /**
     * Method to add two attributes given a name of argument
     * @param argName name of an argument to be added
     */
    private void addArgAtts(String argName) {
        addAttribute(new AttData(argName+ MaeStrings.ID_SUF, true, true));
        addAttribute(new AttData(argName+"Text", true, false));
    }

    /**
     * method to replace a default argument name (arg[0-9]+)
     * with a unique name
     * @param name a unique name for an argument (e.g. sementic roles like agent)
     * @param index index
     */
    public void setArgName(String name, int index) {
        if (!mNary) {
            System.err.println("not a n-ary linking element");
        } else {
            String oldName = mArguments.get(index);
            if (oldName.equals("arg"+index)) {
                removeAttribute(oldName+ MaeStrings.ID_SUF);
                removeAttribute(oldName+"Text");
                mArguments.set(index, name);
                addArgAtts(name);
            } else {
                System.err.println("Selected argument is already has a name");
            }


        }
    }

    public void printInfo() {
        System.out.println("\tname = " + getName());
        // TODO this method needs to re-written from scratch:
        // right now, this method is not used at anywhere so leave it now

    }
}

