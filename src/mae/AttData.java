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
 * A class that describes tag attributes that 
 * only contain text data (such as comments)
 *
 * @author Amber Stubbs, Keigh Rim
 * @version v0.10
 *
 */


class AttData extends Attrib{

AttData(){
}

AttData (String name, boolean r){
    setName(name);
    setRequired(r);
    setData("");
    setDefaultValue("");
}

AttData (String name, boolean r, String d){
    setName(name);
    setRequired(r);
    setData("");
    setDefaultValue(d);
}

public String getData(){
    return data;
}

public void setData(String c){
    data=c;
}

public void printInfo(){
    System.out.println("Attribute name =" + getName() + " \n\trequired = " + getRequired() + "\n\tdata = " + data);
}

public String toString(){
    return("Attribute name =" + getName() + " , required = " + getRequired() + " data = " + data );
}

private String data;

}