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
 * XMLHandler extends the sax DefaultHandler to work specifically with 
 * the stand-off XML format used in MAE.
 * @author Amber Stubbs, Keigh Rim
 * @version v0.12
 *
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;

class XMLHandler extends DefaultHandler {
    private HashCollection<String, Hashtable<String, String>> newTags 
            = new HashCollection<String, Hashtable<String, String>>();
    private boolean mHasText = false;
    private String mText = "";

    XMLHandler() {
    }

    @Override
    public void startElement(
            String nsURI, String strippedName, String tagName, Attributes attribs)
            throws SAXException {

        if (tagName.equalsIgnoreCase("text")) {
            mHasText = true;
        } else {
            Hashtable<String, String> elemInstance = new Hashtable<String, String>();
            for (int i = 0; i < attribs.getLength(); i++) {
                String attName = attribs.getQName(i);
                String attValue = attribs.getValue(i);
                elemInstance.put(attName, attValue);
                // add by krim: for legacy support
            }
            convertLegXml(elemInstance);
            newTags.putEnt(tagName, elemInstance);
        }
    }

    @Override
    public void endElement(String nsURI, String localName, String tagName) {
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (mHasText) {
            mText = new String(ch, start, length);
            mHasText = false;
        }
    }


    HashCollection<String, Hashtable<String, String>> returnTagHash() {
        return newTags;
    }

    public String getText() {
        return mText;
    }

    /**
     * add by krim:
     * Used to convers start-end attributes for old version to new 'spans' attribute.
     *
     * @param elemInstance a HashTable of (attribute, value) entities
     */

    private void convertLegXml(Hashtable<String, String> elemInstance) {

        if (!elemInstance.containsKey("spans")) {
            if (elemInstance.containsKey("start") && elemInstance.containsKey("end")) {
                String start = elemInstance.remove("start");
                String end = elemInstance.remove("end");
                elemInstance.put("spans", start + MaeStrings.SPANDELIMITER + end);
            }
        }
    }
}
