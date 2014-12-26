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
 * Extents Elem to provid information about tags that are 
 * used to label extents in a text (as well as non-conumsing
 * tags).
 * 
 * @author Amber Stubbs
 *
 */



class ElemExtent extends Elem{

ElemExtent(String name, String pre){
    setName(name);
    //extent tags always have id, start, and end
    AttID id = new AttID("id", pre, true);
    AttData start = new AttData("start", true);
    AttData end = new AttData("end", true);
    AttData text = new AttData("text", false);
    addAttribute(id);
    addAttribute(start);
    addAttribute(end);
    addAttribute(text);
}

public void setStart(int s){
    start=s;
}

public int getStart(){
    return start;
}

public void setEnd(int e){
    end=e;
}

public int getEnd(){
    return end;
}


public void printInfo(){
    System.out.println("\tname = " + getName());
    System.out.println("\tStart = " + getStart());
    System.out.println("\tEnd = " + getEnd());
    
}

private int start;
private int end;
}