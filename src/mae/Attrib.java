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
 * The parent class for tag attributes
 * @author Amber Stubbs, Keigh Rim
 * @version v0.10
 *
 */

class Attrib extends Object{

Attrib(){
}


public String getName(){
    return name;
}

public void setName(String n){
    name=n;
}

public boolean getRequired(){
    return required;
}

public void setRequired(boolean r){
    required=r;
}

public String getDefaultValue(){
    return defaultValue;
}

public void setDefaultValue(String d){
    defaultValue = d;
}

public boolean hasDefaultValue(){
    if (defaultValue.length()==0){
        return false;
     }
     return true;
}

/*
public String getType(){
    return type;
}

public void setType(String t){
    type=t;
}
*/
public void printInfo(){
    System.out.println("Attribute name =" + getName() + " , required = " + getRequired());
}

public String toString(){
    return("Attribute name =" + getName() + " , required = " + getRequired() );
}

private String name;
private boolean required;
private String defaultValue;
}