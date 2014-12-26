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
 * XMLHandler extends the sax DefaultHandler to work specifically with 
 * the stand-off XML format used in MAE.
 * 
 * @author Amber Stubbs
 */

import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

class XMLHandler extends DefaultHandler {
    private HashCollection<String,Hashtable<String,String>> newTags = new HashCollection<String,Hashtable<String,String>>();
    private boolean text = false;
    private String textChars="";

    XMLHandler (){
        }


    public void startElement(String nsURI, String strippedName, String tagName, Attributes atts)
       throws SAXException {
           
           if (tagName.equalsIgnoreCase("text")){
               text = true;
           }
           else{
                Hashtable<String,String> tag = new Hashtable<String,String>();
                for(int i=0;i<atts.getLength();i++){
                    String name = atts.getQName(i);
                    String value = atts.getValue(i);
                    tag.put(name,value);
                    newTags.putEnt(tagName,tag);
                }
           }

    }

    public void endElement(String nsURI, String localName, String tagName){
        }


    public void characters(char[] ch, int start, int length) {
       if (text) {
         textChars = new String(ch, start, length);
         text = false;
       }
    }


  HashCollection<String,Hashtable<String,String>> returnTagHash(){
      return newTags;
      }
  
  public String getTextChars(){
      return textChars;
   }
}