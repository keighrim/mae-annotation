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
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.llc.mae.controller;

import edu.brandeis.llc.mae.MaeException;
import edu.brandeis.llc.mae.database.MaeDriverI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Created by krim on 1/2/2016.
 */
public abstract class MaeControllerI {

    public final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    protected JPanel view;
    protected MaeMainController mainController;

    public MaeControllerI(MaeMainController mainController) {
        this.mainController = mainController;
    }

    public MaeMainController getMainController() {
        return mainController;
    }

    public MaeDriverI getDriver() {
        return getMainController().getDriver();
    }

    protected JPanel getView() {
        return view;
    }

    protected MaeControlException catchViewException(String message, Exception e) {
        return new MaeControlException(message, e.getCause());
    }

    protected abstract void addListeners() throws MaeException;
}
