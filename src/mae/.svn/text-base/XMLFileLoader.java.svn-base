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

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * XMLFileLoader reads in any annotated files that are loaded into MAE by 
 * calling the XMLHandler file.
 * 
 * @author Amber Stubbs
 *
 */

class XMLFileLoader{

	private XMLHandler xmlfile;

	XMLFileLoader(File f){
		xmlfile = new XMLHandler();
		try{
			readFile(f);
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}

	private void readFile(File f) throws Exception{

		try { //this will work with java 5 and 6.  Java 1.4 is not supported.

			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(xmlfile);
			String docname = f.toString();
			try{
				parser.parse(docname);
			}catch(Exception ex){
				System.out.println(ex.toString());
				System.out.println("parse of " + docname + " failed");
				throw new Exception();
			}
		}catch (SAXException e) {
			System.out.println("e.toString()");
		}

	}

	public HashCollection<String,Hashtable<String,String>> getTagHash(){
		return xmlfile.returnTagHash();
	}

	public String getTextChars(){
		return xmlfile.getTextChars();
	}


}