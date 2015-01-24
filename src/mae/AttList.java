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

package mae;

/**
 * Used for tag attributes that provide a list of options
 * 
 * @author Amber Stubbs, Keigh Rim
 * @version v0.10
 */


import java.util.ArrayList;

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