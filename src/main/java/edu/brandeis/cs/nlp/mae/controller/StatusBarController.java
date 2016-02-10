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

package edu.brandeis.cs.nlp.mae.controller;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Created by krim on 12/31/2015.
 */
class StatusBarController extends MaeControllerI {

    protected static final Logger logger = LoggerFactory.getLogger(StatusBarController.class);

    private JLabel statusBarLabel;

    StatusBarController(MaeMainController mainController) {
        super(mainController);
        view = new JPanel(new FlowLayout(FlowLayout.LEFT));
        view.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBarLabel = new JLabel();
        view.add(statusBarLabel);
        reset();
    }

    void update() {
        reset();
    }

    @Override
    void addListeners() {
        // no listeners involved

    }

    void setText(String s) {
        statusBarLabel.setText(getModePrefix() + s);
    }

    private String getModePrefix() {
        switch (getMainController().getMode()) {
            case MaeMainController.MODE_MULTI_SPAN:
                return MaeStrings.SB_MSPAN_MODE_PREFIX;
            case MaeMainController.MODE_ARG_SEL:
                return MaeStrings.SB_MARGS_MODE_PREFIX;
            default:
                return "";
        }
    }

    void setEmptySelectionText() {
        switch (getMainController().getMode()) {
            case MaeMainController.MODE_ARG_SEL:
                setText(MaeStrings.SB_MARGS_NOTAG);
                break;
            default:
                setText(MaeStrings.SB_NOTEXT);
        }
    }

    void delayedReset(long delayInMillisecond) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                reset();
            }
        }, delayInMillisecond);

    }

    @Override
    void reset() {

        if (!getMainController().isTaskLoaded()) {
            setText(MaeStrings.SB_NODTD);
        } else if (!getMainController().isDocumentOpen()) {
            setText(MaeStrings.SB_NOFILE);
        } else {
            if (!getMainController().isTextSelected()) {
                setEmptySelectionText();
                return;
            }
            int[] spans = getMainController().getSelectedTextSpans();
            switch (getMainController().getMode()) {
                case MaeMainController.MODE_NORMAL:
                    setText(MaeStrings.SB_TEXT + SpanHandler.convertArrayToString(spans));
                    break;
                case MaeMainController.MODE_MULTI_SPAN:
                    setText(MaeStrings.SB_MSPAN_TEXT + SpanHandler.convertArrayToString(spans));
                    break;
                case MaeMainController.MODE_ARG_SEL:
                    List<ExtentTag> potentialArguments = getMainController().getSelectedArguments();
                    setText(String.format(MaeStrings.SB_MARGS_TAG, potentialArguments.size(), potentialArguments));
                    break;
            }
        }
    }

}
