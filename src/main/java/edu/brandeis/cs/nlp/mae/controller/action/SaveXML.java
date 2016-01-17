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

package edu.brandeis.cs.nlp.mae.controller.action;

import edu.brandeis.cs.nlp.mae.controller.MaeMainController;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Listener for the File menu; determines what action to take for loading/saving
 * documents.
 */
public class SaveXML extends MenuActionI {

    private static String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n<%s>\n";
    // tried embed dtd source in the xml, but that fails XML parser to successfully parse
    // <!DOCTYPE root_element SYSTEM "DTD_filename">\
    // <!DOCTYPE root_element PUBLIC "DTD_name" "DTD_location">
    // see https://xmlwriter.net/xml_guide/doctype_declaration.shtml
    private static String xmlText = "<TEXT><![CDATA[%s]]></TEXT>\n<TAGS>\n";
    private static String xmlTail = "</TAGS>\n</%s>";

    public SaveXML(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            String xmlName = getXMLFileName();
            File file = getMainController().selectSingleFile(xmlName, true);
            if (file != null) {
                getMainController().getDriver().setAnnotationChanged(false);
                getMainController().getDriver().setAnnotationFileName(file.getAbsolutePath());
                OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
                fw.write(generateXMLString());
                fw.close();
            }

            getMainController().showSavedStatus();
        } catch (Exception e) {
            getMainController().showError(e);
        }

    }

    String generateXMLString() throws MaeDBException {
        MaeDriverI driver = getMainController().getDriver();
        String head = String.format(xmlHeader, driver.getTaskFileName(), driver.getTaskName());
        String text = String.format(xmlText, driver.getPrimaryText());
        String tail = String.format(xmlTail, driver.getTaskName());
        String tags = "";
        for (TagType eType : driver.getExtentTagTypes()) {
            for (ExtentTag tag : driver.getAllExtentTagsOfType(eType)) {
                tags += tag.toXmlString() + "\n";
            }
        }
        for (TagType lType : driver.getLinkTagTypes()) {
            for (LinkTag tag : driver.getAllLinkTagsOfType(lType)) {
                tags += tag.toXmlString() + "\n";
            }
        }
        return head + text + tags + tail;
    }

    String getXMLFileName() throws MaeDBException {
        String annotatorSuffix = getMainController().getFilenameSuffix();
        String annotationFileName = getMainController().getDriver().getAnnotationFileName();
        if (annotationFileName.endsWith(".xml")) {
            String baseName = annotationFileName.substring(0, annotationFileName.length() - 4);
            if (baseName.endsWith(annotatorSuffix)) {
                return annotationFileName;
            } else {
                return baseName + annotatorSuffix + ".xml";
            }
        } else {
            return annotationFileName + annotatorSuffix + ".xml";
        }
    }
}

