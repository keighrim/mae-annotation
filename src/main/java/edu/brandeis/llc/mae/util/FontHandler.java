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

package edu.brandeis.llc.mae.util;

import javax.swing.text.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by krim on 2/17/16.
 */
public class FontHandler {

    private static Font[] fontCache = new Font[10];
    private static final Map<Integer, Font> codepointCache = new HashMap<>();

    private static Font getFontToRenderSurrogateCode(int codepoint) {
        if (codepoint >= '\uFE00' && codepoint <= '\uFE0F') {
            return new Font("", 0, 0);
        }

        if (codepointCache.isEmpty()) {
            synchronized (codepointCache) {
                return getRenderableFont(codepoint);
            }
        } else {
            return getRenderableFont(codepoint);
        }
    }

    private static Font getRenderableFont(int codepoint) {

        // first check cached font
        int fontCachingPoint = 0;
        for (int i = 0; i < fontCache.length; i++) {
            Font cached = fontCache[i];
            if (cached != null && cached.canDisplay(codepoint)) {
                return cached;
            } else if (cached == null) {
                fontCachingPoint = i;
                break;
            }
        }

        // then exhaustively search through all system fonts
        if (codepointCache.containsKey(codepoint)) {
            fontCache[fontCachingPoint] = codepointCache.get(codepoint);
            return codepointCache.get(codepoint);
        } else {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (Font font : ge.getAllFonts()) {
                if (!font.getFamily().equals("Apple Color Emoji") && font.canDisplay(codepoint)) {
                    codepointCache.put(codepoint, font);
                    fontCache[fontCachingPoint] = font;
                    return font;
                }
            }
        }
        return new Font("", 0, 0);
    }

    public static boolean containsHighSurrogate(CharSequence sequence) {
        for (int i = 0; i < sequence.length(); i++) {
            if (Character.isHighSurrogate(sequence.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean primaryTextContainsHighSurrogate() {
        return fontCache[0] != null;
    }

    public static StyledDocument stringToSimpleStyledDocument(String plainText, String defaultFontName, int fontSize, Color fontColor) {
        return stringToSimpleStyledDocumentConcurrently(plainText, defaultFontName, fontSize, fontColor);
    }


    private static StyledDocument stringToSimpleStyledDocumentConcurrently(String plainText, String defaultFontName, int fontSize, Color fontColor) {
        StyledDocument document = new DefaultStyledDocument();
        ExecutorService unicodeRenderService = Executors.newCachedThreadPool();

        try {
            if (plainText != null && plainText.length() > 0) {
                document.insertString(0, plainText, StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));

                SimpleAttributeSet aSet = new SimpleAttributeSet();
                StyleConstants.setFontFamily(aSet, defaultFontName);
                StyleConstants.setFontSize(aSet, fontSize);
                StyleConstants.setForeground(aSet, fontColor);

                document.setCharacterAttributes(0, plainText.length(), aSet, false);

                int offset = 0;
                while (offset < plainText.length()) {
                    int length = 1;
                    Character c = plainText.charAt(offset);
                    if (Character.isHighSurrogate(c)) {
                        length = plainText.length() > offset + 2 ? 2 : 1;
                        int finalOffset = offset;
                        int finalLength = length;
                        Thread thread = new Thread(() -> {
                            SimpleAttributeSet unicodeASet = new SimpleAttributeSet();
                            StyleConstants.setFontFamily(unicodeASet,
                                    getFontToRenderSurrogateCode(plainText.codePointAt(finalOffset)).getFontName());
                            document.setCharacterAttributes(finalOffset, finalLength, unicodeASet, false);

                        });
                        unicodeRenderService.submit(thread);
                    }
                    offset += length;
                }
            }
        } catch (BadLocationException ignored) {
        }
        // TODO: 1/23/17 send secondary msg to user that unicode rendering is happening in BG
        unicodeRenderService.shutdown();
        return document;
    }
}
