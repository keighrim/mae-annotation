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
 */

package mae;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * XMLFileLoader reads in any annotated files that are loaded into MAE by 
 * calling the XMLHandler file.
 * 
 * @author Amber Stubbs
 * @revised Keigh Rim
 * 
 */

class XMLFileLoader{

    private XMLHandler mXmlfile;

    XMLFileLoader(File f){
        mXmlfile = new XMLHandler();
        try{
            readFile(f);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void readFile(File f) throws Exception{

        try { //this will work with java 5 and 6.  Java 1.4 is not supported.
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(mXmlfile);
            String docname = f.toString();
            try{
                parser.parse(docname);
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("parse of " + docname + " failed");
                throw new Exception();
            }
        }catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public HashCollection<String,Hashtable<String,String>> getTagHash(){
        return mXmlfile.returnTagHash();
    }

    public String getTextChars(){
        return mXmlfile.getText();
    }
}