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


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Extends Elem; used for describing link tags
 *
 // TODO Remove unused code found by UCDetector
 // ElemLink(String name){
 //     setName(name);
 //     AttID id = new AttID("id", name.substring(0,1), true);
 //     AttData from = new AttData("fromID", true);
 //     AttData fromText = new AttData("fromText",true);
 //     AttData to = new AttData("toID", true);
 //     AttData toText = new AttData("toText",true);
 //     addAttribute(id);
 //     addAttribute(from);
 //     addAttribute(fromText);
 //     addAttribute(to);
 //     addAttribute(toText);
 // }
 * @author Amber Stubbs
 *
 */




class ElemLink extends Elem {

//    private String from;
//    private String fromText;
//    private String to;
//    private String toText;
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
    ElemLink(String name, String idString) {
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
     * Method to add two attributes given a name of argument
     * @param argName
     */
    private void addArgAtts(String argName) {
        addAttribute(new AttData(argName+"ID", true, true));
        addAttribute(new AttData(argName+"Text", true, true));
    }

    /**
     * Retrieve the list of arguments
     * @return
     */
    public ArrayList<String> getArguments() {
        return mArguments;
    }

    /**
     * reset argument attributes, used when set this tag as a n-ary link tag
     */
    public void resetArgAtts() {
        removeAttribute("fromID");
        removeAttribute("fromText");
        removeAttribute("toID");
        removeAttribute("toText");
        mArguments.clear();
    }

    public void addArgement(String argName) {
        // if any arbitrary argument already has been added, just add a new one
        if (mNary) {
            mArguments.add(argName);
            addArgAtts(argName);
        }
        // otherwise, convert this tag to n-ary linking tag and add the first arg
        else {
            resetArgAtts();
            mArguments.add(argName);
            addArgAtts(argName);
        }
    }

    public int getArgNum() {
        return mArguments.size();
    }

    /**
     * method to replace a default argument name (arg[0-9]+)
     * with a unique name
     * @param name a unique name for an argument (e.g.> sementic roles like agent)
     * @param index index
     */
    public void setArgName(String name, int index) {
        if (!mNary) {
            System.out.println("not a n-ary linking element");
        } else {
            String oldName = mArguments.get(index);
            if (oldName.equals("arg"+index)) {
                removeAttribute(oldName+"ID");
                removeAttribute(oldName+"Text");
                mArguments.set(index, name);
                addArgAtts(name);
            } else {
                System.out.println("Selected argument is already has a name");
            }


        }
    }


    /*
    public void setFrom(String f) {
        from = f;
    }

    public String getFrom() {
        return from;
    }

    public void setFromText(String f) {
        fromText = f;
    }

    public String getFromText() {
        return fromText;
    }

    public void setTo(String t) {
        to = t;
    }

    public String getTo() {
        return to;
    }

    public void setToText(String t) {
        toText = t;
    }

    public String getToText() {
        return toText;
    }
    */

    public void printInfo() {
        System.out.println("\tname = " + getName());
        // TODO currently tis method is not used, maybe was written for debugging?

//        System.out.println("\tFromID = " + getFrom());
//        System.out.println("\tToID = " + getTo());

    }
}

// TODO seems done here
