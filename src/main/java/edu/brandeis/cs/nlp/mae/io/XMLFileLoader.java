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

package edu.brandeis.cs.nlp.mae.io;

import edu.brandeis.cs.nlp.mae.util.MappedList;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.util.Hashtable;

/**
 * XMLFileLoader reads in any annotated files that are loaded into MAE by 
 * calling the XMLHandler file.
 * 
 * @author Amber Stubbs, Keigh Rim
 *
 */

public class XMLFileLoader{

    private XMLHandler mXmlfile;

    public XMLFileLoader(File f){
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
            String xmlContents = f.toString();
            try{
                parser.parse(xmlContents);
            }catch(Exception ex){
                ex.printStackTrace();
                System.err.println(String.format(
                        "%s: parsing failed.\n%s", f.getName(), xmlContents));
                throw new Exception();
            }
        }catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public MappedList<String,Hashtable<String,String>> getTagHash(){
        return mXmlfile.returnTagHash();
    }

    public String getTextChars(){
        return mXmlfile.getText();
    }
}