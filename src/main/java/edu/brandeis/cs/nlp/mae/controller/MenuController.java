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
import edu.brandeis.cs.nlp.mae.MaeMain;
import edu.brandeis.cs.nlp.mae.controller.action.*;

import javax.swing.*;
import java.awt.*;

import static edu.brandeis.cs.nlp.mae.MaeHotKeys.*;
import static edu.brandeis.cs.nlp.mae.MaeStrings.*;

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
        modeMenu = createModeMenu();
        displayMenu = createDisplayMenu();
        helpMenu = createHelpMenu();

        menubar.add(fileMenu);
        menubar.add(modeMenu);
        menubar.add(displayMenu);
        menubar.add(helpMenu);

        view.updateUI();

    }

    @Override
    void addListeners() throws MaeException {

    }

    private JMenu createFileMenu() {
        MaeActionI loadTaskAction = new LoadTask(MENUITEM_LOADTASK, null, ksLOADTASK, null, getMainController());
        MaeActionI openFileAction = new OpenFile(MENUITEM_OPENFILE, null, ksOPENFILE, null, getMainController());
        MaeActionI saveXMLAction = new SaveXML(MENUITEM_SAVEXML, null, ksSAVEXML, null, getMainController());
//        MaeActionI closeFileAction = new LoadTask(MENU_FILE_ITEM_CLOSEFILE, null, ksCLOSEFILE, null, getMainController());
        // TODO: 2016-01-10 16:45:38EST add menu item to load gold standard

        JMenu menu = new JMenu(MENU_FILE);
        menu.setMnemonic(MENU_FILE.charAt(0));

        JMenuItem loadTask = new JMenuItem(loadTaskAction);
        JMenuItem openFile = new JMenuItem(openFileAction);
        JMenuItem saveXML = new JMenuItem(saveXMLAction);
//        JMenuItem closeFile = new JMenuItem(closeFileAction);
        boolean taskLoaded = getMainController().isTaskLoaded();
        boolean fileLoaded = getMainController().isDocumentOpen();
        openFile.setEnabled(taskLoaded);
        saveXML.setEnabled(fileLoaded);
//        closeFile.setEnabled(fileLoaded);

        menu.add(loadTask);
        menu.add(openFile);
        menu.addSeparator();
        menu.add(saveXML);
        menu.addSeparator();
//        menu.add(closeFile);
        logger.info("file menu is created: " + menu.getItemCount());
        return menu;
    }

    private JMenu createModeMenu() {
        MaeActionI multiSpanModeAction = new ModeSwitch(MENUITEM_MSPAN_MODE, null, ksMSPANMODE, null, getMainController());
        MaeActionI argSelModeAction = new ModeSwitch(MENUITEM_ARGSEL_MODE, null, ksARGSMODE, null, getMainController());
        MaeActionI normalModeAction = new ModeSwitch(MENUITEM_NORMAL_MODE, null, ksNORMALMODE, null, getMainController());

        JMenu menu = new JMenu(MENU_MODE);
        menu.setMnemonic(MENU_MODE.charAt(0));

        JMenuItem multiSpanMode = new JMenuItem(multiSpanModeAction);
        multiSpanMode.setActionCommand(Integer.toString(MaeMainController.MODE_MULTI_SPAN));
        JMenuItem argSelMode = new JMenuItem(argSelModeAction);
        argSelMode.setActionCommand(Integer.toString(MaeMainController.MODE_ARG_SEL));
        JMenuItem normalMode = new JMenuItem(normalModeAction);
        normalMode.setActionCommand(Integer.toString(MaeMainController.MODE_NORMAL));
        switch (getMainController().getMode()) {
            case MaeMainController.MODE_NORMAL:
                normalMode.setEnabled(false);
                break;
            case MaeMainController.MODE_MULTI_SPAN:
                multiSpanMode.setEnabled(false);
                break;
            case MaeMainController.MODE_ARG_SEL:
                argSelMode.setEnabled(false);
                break;
        }

        menu.add(multiSpanMode);
        menu.add(argSelMode);
        menu.add(normalMode);
        logger.info("mode menu is created: " + menu.getItemCount());
        return menu;
    }

    private JMenu createHelpMenu() {
        MaeActionI aboutAction = new About(MENUITEM_ABOUT, null, ksABOUT, null, getMainController());
        MaeActionI visitWebsiteAction = new VisitWebsite(MENUITEM_WEB, null, ksWEB, null, getMainController());

        JMenu menu = new JMenu(MENU_HELP);
        menu.setMnemonic(MENU_HELP.charAt(0));

        JMenuItem about = new JMenuItem(aboutAction);
        JMenuItem visitWebsite = new JMenuItem(visitWebsiteAction);

        menu.add(about);
        menu.add(visitWebsite);
        logger.info("help menu is created: " + menu.getItemCount());
        return menu;
    }

    private JMenu createDisplayMenu() {
        MaeActionI increaseFontSizeAction = new ChangeFontsize(MENUITEM_ZOOMIN, null, ksZOOMIN, null, getMainController());
        MaeActionI decreaseFontSizeAction = new ChangeFontsize(MENUITEM_ZOOMOUT, null, ksZOOMOUT, null, getMainController());
        MaeActionI resetFontSizeAction = new ChangeFontsize(MENUITEM_RESET_ZOOM, null, ksRESETZOOM, null, getMainController());

        JMenu menu = new JMenu(MENU_DISPLAY);
        menu.setMnemonic(MENU_DISPLAY.charAt(0));

        JMenuItem increaseFontSize = new JMenuItem(increaseFontSizeAction);
        increaseFontSize.setActionCommand("+");
        JMenuItem decreaseFontSize = new JMenuItem(decreaseFontSizeAction);
        decreaseFontSize.setActionCommand("-");
        JMenuItem resetFontSize = new JMenuItem(resetFontSizeAction);
        resetFontSize.setActionCommand("0");

        menu.add(increaseFontSize);
        menu.add(decreaseFontSize);
        menu.add(resetFontSize);
        logger.info("display menu is created: " + menu.getItemCount());
        return menu;
    }

}
