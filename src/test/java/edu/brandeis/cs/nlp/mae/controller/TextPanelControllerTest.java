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

package edu.brandeis.cs.nlp.mae.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.text.StyledDocument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by krim on 1/3/2016.
 */
public class TextPanelControllerTest {

    MaeMainController mainController;
    TextPanelController controller;

    @Before
    public void setUp() throws Exception {
        mainController = new MaeMainController();
        controller = new TextPanelController(mainController);

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void canAddDocument() throws Exception {

        controller.addDocument("DOC1", "0123456789");

        assertEquals(
                "Expected one document is added, found: " + controller.getView().getTabs().getTabCount(),
                1, controller.getView().getTabs().getTabCount()
        );
        assertEquals(
                "Expected the first tab has title \"DOC1\", found: " + controller.getView().getTabs().getTitleAt(0),
                "DOC1", controller.getView().getTabs().getTitleAt(0)
        );

        assertTrue(
                "Expected text controller can retrieve the whole text, found: " + controller.getText(),
                controller.getText().equals("0123456789")
        );
        String z = controller.getTextBetween(0, 3, false);
        assertTrue(
                "Expected text controller can retrieve a span [0-3) of text, found: " + z,
                z.equals("012")
        );
    }

    @Test
    public void canCloseDocument() throws Exception {
        controller.addDocument("DOC1", "0123456789");
        assertEquals(
                "Expected one document is added, found: " + controller.getView().getTabs().getTabCount(),
                1, controller.getView().getTabs().getTabCount()
        );

        controller.closeDocument(0);
        assertEquals(
                "Expected the document is now closed, found: " + controller.getView().getTabs().getTabCount(),
                0, controller.getView().getTabs().getTabCount()
        );

    }

    @Test
    public void canTrackTextSelection() throws Exception {
        controller.addDocument("DOC1", "012345");
        controller.addSelection(new int[]{0, 3});
        assertTrue(
                "Expected span [0,3) is selected, found: " + controller.getTextInSelected(),
                controller.getTextInSelected().equals("012")
        );
        controller.addSelection(new int[]{2, 6});
        assertTrue(
                "Expected now span [0,6) is selected, found: " + controller.getTextInSelected(),
                controller.getTextInSelected().equals("012345")
        );


    }
}