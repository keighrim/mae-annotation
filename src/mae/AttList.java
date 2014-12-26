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
 * Used for tag attributes that provide a list of options
 * 
 * @author Amber Stubbs
 */


import java.util.*;

class AttList extends Attrib{

AttList(){
}


AttList (String name, boolean r, ArrayList<String> c, String d){
    setName(name);
    setRequired(r);
    setList(c);
    setDefaultValue(d);
}

public ArrayList<String> getList(){
    return list;
}

public void setList(ArrayList<String> l){
    list=l;
}

public String toString(){
    return("Attribute name =" + getName() + " , required = " + getRequired() + "also list" );
}


private ArrayList<String> list;

}