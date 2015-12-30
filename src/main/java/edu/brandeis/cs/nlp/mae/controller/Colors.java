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

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;

/**
 * Created by krim on 11/17/15.
 */
public class Colors {

    //Here is where to change the colors that get assigned to tags
    // these are for text colors
    protected static final Color mRed = new Color(255, 0, 0);
    protected static final Color mLightBlue = new Color(11, 162, 188);
    protected static final Color mOrange = new Color(234, 160, 0);
    protected static final Color mDarkGreen = new Color(12, 153, 72);
    protected static final Color mMagenta = new Color(255, 0, 255);
    protected static final Color mDarkBlue = new Color(42, 92, 140);
    protected static final Color mYellow = new Color(255, 255, 0);
    protected static final Color mPurple = new Color(150, 20, 120);
    protected static final Color mGray = new Color(200, 200, 200);
    protected static final Color mViolet = new Color(102, 75, 153);
    protected static final Color mGold = new Color(207, 181, 59);
    protected static final Color mBlue = new Color(0, 0, 255);
    protected static final Color mDarkOrange = new Color(153, 102, 0);

    protected static Color[] mColors = {
            mRed, mLightBlue, mOrange, mDarkGreen, mMagenta, mDarkBlue,
            mYellow, mPurple, mGray, mViolet, mGold, mBlue, mDarkOrange};

    // thses are for highlighter colors
    protected static final Color mLightOrange = new Color(255, 204, 51);
    protected static final Color mGreen = Color.green;
    protected static final Color mPink = Color.pink;
    protected static final Color mCyan = Color.cyan;
    protected static final Color mLightGray = Color.lightGray;

    protected static final TextHighlightPainter mOrangeHL = new TextHighlightPainter(mLightOrange);
    protected static final TextHighlightPainter mGreenHL = new TextHighlightPainter(mGreen);
    protected static final TextHighlightPainter mPinkHL = new TextHighlightPainter(mPink);
    protected static final TextHighlightPainter mCyanHL = new TextHighlightPainter(mCyan);
    protected static final TextHighlightPainter mGrayHL = new TextHighlightPainter(mLightGray);
    protected static final Highlighter.HighlightPainter mDefHL = DefaultHighlighter.DefaultPainter;

    public static TextHighlightPainter getFadingHighlighter() {
        return mGrayHL;
    }


    public static Highlighter.HighlightPainter getDefaultHighliter() {
        return mDefHL;
    }

    public static Highlighter.HighlightPainter getVividHighliter() {
        return mOrangeHL;
    }


    // default color is excluded from the list; it's for indicating selection
    protected static TextHighlightPainter[] mHighlighters = {
            mOrangeHL, mGreenHL, mPinkHL, mCyanHL, mGrayHL};

    public static TextHighlightPainter[] getHighlighters() {
        return mHighlighters;
    }

    public static Color[] getColors() {
        return mColors;
    }

}
