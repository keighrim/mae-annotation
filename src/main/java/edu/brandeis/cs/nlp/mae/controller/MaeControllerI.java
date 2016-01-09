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

import javax.swing.*;
import java.awt.*;

/**
 * Created by krim on 1/2/2016.
 */
public abstract class MaeControllerI {
    protected JPanel view;
    protected MaeMainController mainController;

    public MaeControllerI(Container mainController) {
        try {
            this.mainController = (MaeMainController) mainController;
        } catch (ClassCastException e) {
            // TODO: 1/1/2016 this means the bar is not attached to MainFrame, statbar needs to be init with maincontroller
            e.printStackTrace();
        }
    }

    MaeMainController getMainController() {
        return mainController;
    }

    JPanel getView() {
        return view;
    }

    MaeControlException catchViewException(String message, Exception e) {
        return new MaeControlException(message, e.getCause());
    }

    abstract void reset() throws MaeException;

    abstract void addListeners() throws MaeException;
}