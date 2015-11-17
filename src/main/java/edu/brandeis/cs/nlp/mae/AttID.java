
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

package edu.brandeis.cs.nlp.mae;

/**
 * 
 * ID attributes have special properties, so they 
 * have their own class that keeps track of the 
 * prefix.
 * @author Amber Stubbs, Keigh Rim
 *
 */


class AttID extends Attrib{

AttID(){
}

AttID (String name, String pre, boolean r){
    setName(name);
    setRequired(r);
    setPrefix(pre);
    setDefaultValue("");
}

public String getID(){
   return(prefix + Integer.toString(number));
}

public void setPrefix(String pre){
    prefix=pre;
}

public String getPrefix(){
    return prefix;
}

public void setNumber(int i){
    number=i;
}

public int getNumber(){
    return number;
}

public void incrementNumber(){
    number++;
}

public void printInfo(){
    System.out.println("Attribute name =" + getName() + " , required = " + isRequired());
}

public String toString(){
    return("Attribute name =" + getName() + " , required = " + isRequired() );
}

private String prefix;
private int number;
}