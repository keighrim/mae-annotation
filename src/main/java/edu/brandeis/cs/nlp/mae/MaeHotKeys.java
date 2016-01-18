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

package edu.brandeis.cs.nlp.mae;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Contains hotkeys for MAE main
 * Created by krim on 2/17/2015.
 * @author Keigh Rim
 *
 */
public class MaeHotKeys {
    
    // function keys
    public final static KeyStroke ksF1
            = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
    public final static KeyStroke ksF2
            = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
    public final static KeyStroke ksF3
            = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
    public final static KeyStroke ksF4
            = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
    public final static KeyStroke ksF5
            = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
    public final static KeyStroke ksF6
            = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
    public final static KeyStroke ksF7
            = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
    public final static KeyStroke ksF8
            = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
    public final static KeyStroke ksF9
            = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
    public final static KeyStroke ksF10
            = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
    public final static KeyStroke ksF11
            = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
    public final static KeyStroke ksF12
            = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
    
    // number keys
    public final static int n1 = KeyEvent.VK_1;
    public final static int n2 = KeyEvent.VK_2;
    public final static int n3 = KeyEvent.VK_3;
    public final static int n4 = KeyEvent.VK_4;
    public final static int n5 = KeyEvent.VK_5;
    public final static int n6 = KeyEvent.VK_6;
    public final static int n7 = KeyEvent.VK_7;
    public final static int n8 = KeyEvent.VK_8;
    public final static int n9 = KeyEvent.VK_9;
    public final static int n0 = KeyEvent.VK_0;
    public final static int[] numKeys = new int[] {
            n1, n2, n3, n4, n5, n6, n7, n8, n9, n0};

    public final static KeyStroke ksC1
            = KeyStroke.getKeyStroke(n1, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC2
            = KeyStroke.getKeyStroke(n2, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC3
            = KeyStroke.getKeyStroke(n3, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC4
            = KeyStroke.getKeyStroke(n4, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC5
            = KeyStroke.getKeyStroke(n5, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC6
            = KeyStroke.getKeyStroke(n6, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC7
            = KeyStroke.getKeyStroke(n7, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC8
            = KeyStroke.getKeyStroke(n8, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC9
            = KeyStroke.getKeyStroke(n9, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksC0
            = KeyStroke.getKeyStroke(n0, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    
    public final static KeyStroke[] ctrlNums = new KeyStroke[] {
            ksC1, ksC2, ksC3, ksC4, ksC5, ksC6, ksC7, ksC8, ksC9, ksC0 };
    
    public final static KeyStroke ksA1
            = KeyStroke.getKeyStroke(n1, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA2
            = KeyStroke.getKeyStroke(n2, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA3
            = KeyStroke.getKeyStroke(n3, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA4
            = KeyStroke.getKeyStroke(n4, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA5
            = KeyStroke.getKeyStroke(n5, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA6
            = KeyStroke.getKeyStroke(n6, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA7
            = KeyStroke.getKeyStroke(n7, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA8
            = KeyStroke.getKeyStroke(n8, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA9
            = KeyStroke.getKeyStroke(n9, KeyEvent.ALT_DOWN_MASK);
    public final static KeyStroke ksA0
            = KeyStroke.getKeyStroke(n0, KeyEvent.ALT_DOWN_MASK);
    
    public final static KeyStroke[] altNums = new KeyStroke[] {
            ksA1, ksA2, ksA3, ksA4, ksA5, ksA6, ksA7, ksA8, ksA9, ksA0 };
    
    public final static KeyStroke ksN1
            = KeyStroke.getKeyStroke(n1, 0);
    public final static KeyStroke ksN2
            = KeyStroke.getKeyStroke(n2, 0);
    public final static KeyStroke ksN3
            = KeyStroke.getKeyStroke(n3, 0);
    public final static KeyStroke ksN4
            = KeyStroke.getKeyStroke(n4, 0);
    public final static KeyStroke ksN5
            = KeyStroke.getKeyStroke(n5, 0);
    public final static KeyStroke ksN6
            = KeyStroke.getKeyStroke(n6, 0);
    public final static KeyStroke ksN7
            = KeyStroke.getKeyStroke(n7, 0);
    public final static KeyStroke ksN8
            = KeyStroke.getKeyStroke(n8, 0);
    public final static KeyStroke ksN9
            = KeyStroke.getKeyStroke(n9, 0);
    public final static KeyStroke ksN0
            = KeyStroke.getKeyStroke(n0, 0);

    public final static KeyStroke[] noneNums = new KeyStroke[] {
            ksN1, ksN2, ksN3, ksN4, ksN5, ksN6, ksN7, ksN8, ksN9, ksN0 };

    // context menu keys
    public final static KeyStroke ksNZ
            = KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0);
    public final static KeyStroke ksUNDO = ksNZ;
    
    public final static KeyStroke ksNS
            = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
    public final static KeyStroke ksSTARTOVER = ksNS;
    
    public final static KeyStroke ksND
            = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
    public final static KeyStroke ksDELETE = ksND;
    public final static int cmnDELETE = KeyEvent.VK_D;

    public final static KeyStroke ksNE
            = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
    public final static KeyStroke ksNA
            = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);

    // menu mnemonics
    public final static int mnFILEMENU = KeyEvent.VK_F;
    public final static int mnTAGMENU = KeyEvent.VK_C;
    public final static int mnLINKMENU = KeyEvent.VK_L;
    public final static int mnLINKARGMENU = KeyEvent.VK_K;
    public final static int mnSETARGMENU = KeyEvent.VK_S;
    public final static int mnNCMENU = KeyEvent.VK_N;
    public final static int mnMODEMENU = KeyEvent.VK_M;
    public final static int mnDPMENU = KeyEvent.VK_D;
    public final static int mnPREFMENU = KeyEvent.VK_P;
    public final static int mnHELPMENU = KeyEvent.VK_H;
    public final static int mnOK_BUTTON = KeyEvent.VK_O;
    public final static int mnCANCEL_BUTTON = KeyEvent.VK_C;
    
    // file menu keys
    public final static KeyStroke ksCN
            = KeyStroke.getKeyStroke(KeyEvent.VK_N, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksLOADTASK = ksCN;
    
    public final static KeyStroke ksCO
            = KeyStroke.getKeyStroke(KeyEvent.VK_O,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksOPENFILE = ksCO;

    public final static KeyStroke ksCI
            = KeyStroke.getKeyStroke(KeyEvent.VK_I,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksADDFILE = ksCI;

    public final static KeyStroke ksCR
            = KeyStroke.getKeyStroke(KeyEvent.VK_R, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksSAVERTF = ksCR;

    public final static KeyStroke ksCLOSEFILE = ksF4;

    public final static KeyStroke ksCS
            = KeyStroke.getKeyStroke(KeyEvent.VK_S, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksSAVEXML = ksCS;
    
    // display menu keys
    public final static KeyStroke ksCMinus
            = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksFONTSMALL = ksCMinus;
    
    public final static KeyStroke ksCEquals
            = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksFONTBIG = ksCEquals;
    
    public final static KeyStroke ksCPlus
            = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

    // TODO: 2016-01-10 16:36:05EST for side panel support
    public final static KeyStroke SIDEPANEL = ksF9;
    
    // mode menu keys
    public final static KeyStroke ksCE
            = KeyStroke.getKeyStroke(KeyEvent.VK_E, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    public final static KeyStroke ksNORMALMODE = ksCE;
    public final static KeyStroke ksMSPANMODE = ksC1;
    public final static KeyStroke ksARGSMODE = ksC2;
    public final static KeyStroke ksADJUDMODE = ksC9;

    // help menu keys
    public final static  KeyStroke ksABOUT = ksF10;
    public final static  KeyStroke ksWEB = ksF1;
    
    // switch between tabs
    public final static KeyStroke ksCTab
            = KeyStroke.getKeyStroke("ctrl Tab");
    public final static KeyStroke ksCShTab
            = KeyStroke.getKeyStroke("ctrl shift Tab");
    public final static KeyStroke ksCPgup
            = KeyStroke.getKeyStroke("ctrl PgUp");
    public final static KeyStroke ksCPgdn
            = KeyStroke.getKeyStroke("ctrl PgDown");
    // TODO: 2016-01-10 16:37:12EST make separate keyboard shorcuts for tab navigation of text and tabel panels
    public final static KeyStroke NEXTTAB = ksCTab;
    public final static KeyStroke PREVTAB = ksCShTab;
    public final static KeyStroke TABUP = ksCPgup;
    public final static KeyStroke TABDOWN = ksCPgdn;
}

