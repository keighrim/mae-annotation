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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For feedback, reporting bugs, use the project repo on github
 * <https://github.com/keighrim/mae-annotation>
 *
 * @author Amber Stubss, Keigh Rim
 * @version v0.11
 */

package mae;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Provides methods for loading a DTD file into a DTD class
 * 
 * @see DTD
 */

class DTDLoader {

    private DTD mDtd;
    
    DTDLoader(File f){
        mDtd =new DTD();
        try{
            readFile(f);
        }catch(FileNotFoundException e){
            System.out.println("no file found");
        }
    }
    
    public DTD getDTD(){
        return mDtd;
    }
    
    private void readFile(File f) throws FileNotFoundException{
      Scanner sc = new Scanner(f,"UTF-8");
      while (sc.hasNextLine()) {
          String next = sc.nextLine();
            //first, get rid of comments
            //this assumes that comments are on their own line(s)
            //needs to be made more flexible
            if (next.contains("<!--")){
                while (!next.contains("-->")){
                    next = sc.nextLine();
                }
                //this skips the lines with the comments
                next= sc.nextLine();
            }

            //then, get all information about a tag into one string
            String tag = "";
            if (next.contains("<")){
                tag = tag+next;
                while (!next.contains(">")){
                    next = sc.nextLine();
                    tag = tag+next;
                }
            }
            tag = tag.replaceAll(" +"," ");
            process(tag);
      } 
    }
    
    private void process(String tag){
        
        if(tag.startsWith("<!ELEMENT")){
            createElement(tag);
        }
        
        if(tag.startsWith("<!ATTLIST")){
            addAttribute(tag);
        }
        
        if(tag.startsWith("<!ENTITY")){
            addMeta(tag);
        }
    }
    
    /*
    Create a new element in the DTD
    */
    private void createElement(String tag){
        String name = tag.split(" ")[1];
        // PCDATA indicating this is an extend tag
        if (tag.contains("#PCDATA")){
            String idString = getIDString(name);
            ElemExtent e = new ElemExtent(name, idString);
            mDtd.addElem(e);
        }
        // else, that is a link tag
        else {
            String idString = getIDString(name);
            ElemLink e = new ElemLink(name, idString);
            mDtd.addElem(e);
        }
    }
    
    private String getIDString(String name){
        ArrayList<String> ids = mDtd.getElementIDs();
        String id = name.substring(0,1);
        boolean idOkay = false;
        while (!idOkay){
            if(ids.contains(id)){
                if(id.length()>=name.length()){
                    id = id+"-";
                 } else {
                     id = name.substring(0,id.length()+1);
                 }
            } else {
                idOkay=true;
            }
        }
        return id;
    }
    
    private void addMeta(String tag){
        if (tag.contains("name ")){
            String name= tag.split("name \"")[1];
            name = name.split("\"")[0];
            mDtd.setName(name);
        }
    }
    
    /**
     * Add an attribute to an existing string
     */
    private void addAttribute(String tag){
         if (tag.contains("(")) {
             addListAtt(tag);
         } else {
             addDataAtt(tag);
         }
    }

    /**
     * Create an attribute with a list of valid values
     * @param tag
     */
    private void addListAtt(String tag){
        String elemName = tag.split(" ")[1];
        String attName = tag.split(" ")[2];
        Elem elem = mDtd.getElem(elemName);
        
        if(elem!=null){
            String listString = tag.split("\\(")[1];
            listString = listString.split("\\)")[0];

            ArrayList<String> validValues = new ArrayList<String>();
            for (String value : listString.split("\\|")) {
                validValues.add(value.trim());
            }
                
//            ArrayList<String> validValues
//                    = new ArrayList<String>(Arrays.asList(listString.split(" \\| ")));
//            for (String value : validValues) {
//                value = value.trim();
//            }

             Pattern defValPat = Pattern.compile("\"[\\w ]+\" *>");
             Matcher matcher = defValPat.matcher(tag);
             ArrayList<String> defVal = new ArrayList<String>();
             String defaultValue = "";
             while (matcher.find()){
                 defVal.add(matcher.group());
             }
             if (defVal.size()>1){
                 System.out.println("Error in attribute; too many default values found");
                 System.out.println(tag);
             }
             else if (defVal.size()==1){
                 defaultValue = defVal.get(0).split("\"")[1];
                 if (!validValues.contains(defaultValue)){
                     System.out.println("Error -- default value not in attribute list");
                     System.out.println(tag);
                     defaultValue="";
                 }
             }
             boolean req = tag.contains("#REQUIRED");
             elem.addAttribute(new AttList(attName,req,validValues,defaultValue));
         }
         else{
             System.out.println("no match found: '" + elemName + "' is not a valid tag identifier");
         }
    }
        
    /**
     * Creates an attribute that can have an arbitrary string data
     */
    private void addDataAtt(String tag){

        String elemName = tag.split(" ")[1];
        String attName = tag.split(" ")[2];
        boolean req = tag.contains("#REQUIRED");
        if(mDtd.hasElem(elemName)){
            Elem elem = mDtd.getElem(elemName);

            // krim: support for multi-span extents
            // dropped "end" tag, kept "start" only
            // keeping "start" is for legacy DTD support
            // (instead of replacing it with the actually used name "spans")
            if(attName.equalsIgnoreCase("start")
                    || attName.equalsIgnoreCase("spans")){
                if(elem instanceof ElemExtent){
                    Attrib att = elem.getAttribute("spans");
                    att.setRequired(req);
                }
            } else if(tag.contains(" ID ")) {
                AttID att = (AttID) elem.getAttribute("id");
                if (tag.contains("prefix")) {
                    String prefix = tag.split("\"")[1];
                    att.setPrefix(prefix);
                }
            } else {
                // added by krim: for multi-link support
                // first check if this att is for argument
                Pattern argAttPat = Pattern.compile("^arg[0-9]+$");
                Matcher matcher = argAttPat.matcher(attName);
                if (matcher.find()) {
                    // then check elem is a link tag
                    if (elem instanceof ElemLink) {
                        String argName;
                        // name argument if a name is given
                        if (tag.contains("prefix")) {
                            argName = tag.split("\"")[1];
                        } 
                        // otherwise, use argN format as a default name
                        else {
                            argName = matcher.group();
                        }
                        ((ElemLink) elem).addArgement(argName);
                        // then adjust max args in dtd object
                        if (mDtd.getMaxArgs() < ((ElemLink) elem).getArgNum()) {
                            mDtd.setMaxArgs(((ElemLink) elem).getArgNum());
                        }
                    } else {
                        System.out.println("No argument attrib allowed for an extend tag");
                    }

                }
                // otherwise, add as a simple data attrib (original code)
                else {
                    Pattern defaultVal = Pattern.compile("\"[\\w ]+\" *>");
                    matcher = defaultVal.matcher(tag);
                    ArrayList<String> defVals = new ArrayList<String>();
                    String defaultValue = "";
                    while (matcher.find()) {
                        defVals.add(matcher.group());
                    }
                    if (defVals.size() > 1) {
                        System.out.println("Error in attribute; too many default values found");
                        System.out.println(tag);
                    } else if (defVals.size() == 1) {
                        defaultValue = defVals.get(0).split("\"")[1];
                    }
                    AttData att = (new AttData(attName, req, defaultValue));
                    // added by krim: check for IDREF for UI improvement
                    att.setIdRef(tag.contains("IDREF"));
                    elem.addAttribute(att);
                }
            }
        }
        else{
            System.out.printf("element name %s is not found", elemName);
        }
    }
}
