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

package edu.brandeis.cs.nlp.mae.util;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by krim on 11/17/15.
 */
public class ColorHandler {

    private int paletteSize;
    private List<Color> colors;
    public static ArrayList<Color> base = new ArrayList<Color>() {{
            add(new Color(228, 3, 3));
            add(new Color(255, 140, 0));
            add(new Color(255, 237, 0));
            add(new Color(0, 128, 38));
            add(new Color(0, 77, 255));
            add(new Color(117, 7, 135));
        }};

    public ColorHandler(int size) {
        paletteSize = size;
        if (size <= 5) {
            colors = base.subList(0, size);
        } else {
            colors = new ArrayList<>(base);
            while (colors.size() < size) {
                float r = 0;
                float g = 0;
                float b = 0;
                while (r < 0.3 && g < 0.3 && b < 0.3) {
                    r = ThreadLocalRandom.current().nextFloat();
                    g = ThreadLocalRandom.current().nextFloat();
                    b = ThreadLocalRandom.current().nextFloat();
                }
                colors.add(new Color(r, g, b));
            }
        }
    }

    public static Highlighter.HighlightPainter getDefaultHighlighter() {
        return DefaultHighlighter.DefaultPainter;
    }

    public static Highlighter.HighlightPainter getFadingHighlighter() {
        return new DefaultHighlighter.DefaultHighlightPainter(Color.lightGray);
    }

    public static Highlighter.HighlightPainter getVividHighliter() {
        return new DefaultHighlighter.DefaultHighlightPainter(new Color(90, 220, 30));
    }

    public static Highlighter.HighlightPainter getCustomHighliter(Color color) {
        return new DefaultHighlighter.DefaultHighlightPainter(color);
    }

    public List<Color> getColors() {
        return this.colors;
    }

    public static List<Color> getBaseColors() {
        return base;
    }

    public Color getColor(int i) {
        return colors.get(i);
    }


}
