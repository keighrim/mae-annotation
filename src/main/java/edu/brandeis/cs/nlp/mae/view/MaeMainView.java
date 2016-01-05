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

package edu.brandeis.cs.nlp.mae.view;

import edu.brandeis.cs.nlp.mae.MaeStrings;

import javax.swing.*;
import java.awt.*;

/**
 * Created by krim on 1/2/2016.
 */
public class MaeMainView extends JFrame {


    public MaeMainView(JPanel menuBarView, JPanel textPanelView, JPanel statusBarView, JPanel tablePanelView) {
        super(MaeStrings.TITLE_PREFIX);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(900, 500));

        JPanel root = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JPanel bottom = tablePanelView;

        top.add(textPanelView, BorderLayout.CENTER);
        top.add(statusBarView, BorderLayout.SOUTH);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        main.setDividerLocation(250);

        root.add(menuBarView, BorderLayout.NORTH);
        root.add(main, BorderLayout.CENTER);

        setContentPane(root);
    }
}
