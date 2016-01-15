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

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.controller.action.*;
import edu.brandeis.cs.nlp.mae.controller.menu.MaeActionI;

import static edu.brandeis.cs.nlp.mae.MaeStrings.*;
import static edu.brandeis.cs.nlp.mae.MaeHotKeys.*;

import javax.swing.*;
import java.awt.*;

/**
 * Created by krim on 1/2/2016.
 */
public class MenuController extends MaeControllerI {

    // this controller is responsible for all these menus
    JMenu fileMenu;
    JMenu displayMenu;
    JMenu helpMenu;
    JMenu modeMenu;
    JMenu preferenceMenu;
    JMenu textContextMenu;
    JMenu tableContextMenu;


    // and this view for top main menu
    private JMenuBar menubar;

    public MenuController(MaeMainController mainController) {
        super(mainController);
        view = new JPanel(new BorderLayout());
        menubar = new JMenuBar();
        reset();
        view.add(menubar, BorderLayout.CENTER);

    }

    @Override
    void reset() {

        menubar.removeAll();

        fileMenu = createFileMenu();
        menubar.add(fileMenu);

        view.updateUI();

    }

    @Override
    void addListeners() throws MaeException {

    }

    private JMenu createFileMenu() {
        MaeActionI loadTaskAction = new LoadTask(MENU_FILE_ITEM_LOADTASK, null, ksLOADTASK, null, getMainController());
        MaeActionI openFileAction = new OpenFile(MENU_FILE_ITEM_OPENFILE, null, ksOPENFILE, null, getMainController());
//        MaeActionI saveXMLAction = new SaveXML(MENU_FILE_ITEM_SAVEXML, null, ksSAVEXML, null, getMainController());
//        MaeActionI saveRTFAction = new SaveRTF(MENU_FILE_ITEM_SAVERTF, null, ksSAVERTF, null, getMainController());
//        MaeActionI closeFileAction = new LoadTask(MENU_FILE_ITEM_CLOSEFILE, null, ksCLOSEFILE, null, getMainController());
        // TODO: 2016-01-10 16:45:38EST add menu item to load gold standard

        JMenu menu = new JMenu(MENU_FILE);

        JMenuItem loadTask = new JMenuItem(loadTaskAction);
        JMenuItem openFile = new JMenuItem(openFileAction);
//        JMenuItem saveXML = new JMenuItem(saveXMLAction);
//        JMenuItem saveRTF = new JMenuItem(saveRTFAction);
//        JMenuItem closeFile = new JMenuItem(closeFileAction);
        boolean taskLoaded = getMainController().isTaskLoaded();
        boolean fileLoaded = getMainController().isDocumentOpen();
        openFile.setEnabled(taskLoaded);
//        saveXML.setEnabled(fileLoaded);
//        saveRTF.setEnabled(fileLoaded);
//        closeFile.setEnabled(fileLoaded);

        menu.add(loadTask);
        menu.add(openFile);
        menu.addSeparator();
//        menu.add(saveXML);
//        menu.add(saveRTF);
        menu.addSeparator();
//        menu.add(closeFile);
        logger.info("file menu is created: " + menu.getItemCount());
        return menu;
    }

}
