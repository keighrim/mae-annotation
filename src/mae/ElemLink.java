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




class ElemLink extends Elem{

ElemLink(){
}


ElemLink(String name, String pre){
    setName(name);
    AttID id = new AttID("id", pre, true);
    AttData from = new AttData("fromID", true);
    AttData fromText = new AttData("fromText",true);
    AttData to = new AttData("toID", true);
    AttData toText = new AttData("toText",true);
    addAttribute(id);
    addAttribute(from);
    addAttribute(fromText);
    addAttribute(to);
    addAttribute(toText);
}

public void setFrom(String f){
    from=f;
}

public String getFrom(){
    return from;
}

public void setFromText(String f){
    fromText=f;
}

public String getFromText(){
    return fromText;
}

public void setTo(String t){
    to=t;
}

public String getTo(){
    return to;
}

public void setToText(String t){
    toText=t;
}

public String getToText(){
    return toText;
}



public void printInfo(){
    System.out.println("\tname = " + getName());
    System.out.println("\tFromID = " + getFrom());
    System.out.println("\tToID = " + getTo());
    
}

private String from;
private String fromText;
private String to;
private String toText;

}
