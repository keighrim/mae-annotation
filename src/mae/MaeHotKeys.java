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

package mae;

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
    final static KeyStroke ksF1
            = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
    final static KeyStroke ksF2
            = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
    final static KeyStroke ksF3
            = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
    final static KeyStroke ksF4
            = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
    final static KeyStroke ksF5
            = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
    final static KeyStroke ksF6
            = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
    final static KeyStroke ksF7
            = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
    final static KeyStroke ksF8
            = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
    final static KeyStroke ksF9
            = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
    final static KeyStroke ksF10
            = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
    final static KeyStroke ksF11
            = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
    final static KeyStroke ksF12
            = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
    
    // number keys
    final static int n1 = KeyEvent.VK_1;
    final static int n2 = KeyEvent.VK_2;
    final static int n3 = KeyEvent.VK_3;
    final static int n4 = KeyEvent.VK_4;
    final static int n5 = KeyEvent.VK_5;
    final static int n6 = KeyEvent.VK_6;
    final static int n7 = KeyEvent.VK_7;
    final static int n8 = KeyEvent.VK_8;
    final static int n9 = KeyEvent.VK_9;
    final static int n0 = KeyEvent.VK_0;
    final static int[] numKeys = new int[] {
            n1, n2, n3, n4, n5, n6, n7, n8, n9, n0};

    final static KeyStroke ksC1
            = KeyStroke.getKeyStroke(n1, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC2
            = KeyStroke.getKeyStroke(n2, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC3
            = KeyStroke.getKeyStroke(n3, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC4
            = KeyStroke.getKeyStroke(n4, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC5
            = KeyStroke.getKeyStroke(n5, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC6
            = KeyStroke.getKeyStroke(n6, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC7
            = KeyStroke.getKeyStroke(n7, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC8
            = KeyStroke.getKeyStroke(n8, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC9
            = KeyStroke.getKeyStroke(n9, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ksC0
            = KeyStroke.getKeyStroke(n0, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    
    final static KeyStroke[] ctrlNums = new KeyStroke[] {
            ksC1, ksC2, ksC3, ksC4, ksC5, ksC6, ksC7, ksC8, ksC9, ksC0 };
    
    final static KeyStroke ksA1
            = KeyStroke.getKeyStroke(n1, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA2
            = KeyStroke.getKeyStroke(n2, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA3
            = KeyStroke.getKeyStroke(n3, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA4
            = KeyStroke.getKeyStroke(n4, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA5
            = KeyStroke.getKeyStroke(n5, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA6
            = KeyStroke.getKeyStroke(n6, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA7
            = KeyStroke.getKeyStroke(n7, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA8
            = KeyStroke.getKeyStroke(n8, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA9
            = KeyStroke.getKeyStroke(n9, KeyEvent.ALT_DOWN_MASK);
    final static KeyStroke ksA0
            = KeyStroke.getKeyStroke(n0, KeyEvent.ALT_DOWN_MASK);
    
    final static KeyStroke[] altNums = new KeyStroke[] {
            ksA1, ksA2, ksA3, ksA4, ksA5, ksA6, ksA7, ksA8, ksA9, ksA0 };
    
    final static KeyStroke ksN1
            = KeyStroke.getKeyStroke(n1, 0);
    final static KeyStroke ksN2
            = KeyStroke.getKeyStroke(n2, 0);
    final static KeyStroke ksN3
            = KeyStroke.getKeyStroke(n3, 0);
    final static KeyStroke ksN4
            = KeyStroke.getKeyStroke(n4, 0);
    final static KeyStroke ksN5
            = KeyStroke.getKeyStroke(n5, 0);
    final static KeyStroke ksN6
            = KeyStroke.getKeyStroke(n6, 0);
    final static KeyStroke ksN7
            = KeyStroke.getKeyStroke(n7, 0);
    final static KeyStroke ksN8
            = KeyStroke.getKeyStroke(n8, 0);
    final static KeyStroke ksN9
            = KeyStroke.getKeyStroke(n9, 0);
    final static KeyStroke ksN0
            = KeyStroke.getKeyStroke(n0, 0);

    final static KeyStroke[] noneNums = new KeyStroke[] {
            ksN1, ksN2, ksN3, ksN4, ksN5, ksN6, ksN7, ksN8, ksN9, ksN0 };

    // context menu keys
    final static KeyStroke ksNZ
            = KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0);
    final static KeyStroke UNDO = ksNZ;
    
    final static KeyStroke ksNS
            = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
    final static KeyStroke STARTOVER = ksNS;
    
    final static KeyStroke ksND
            = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
    final static KeyStroke DELETE = ksND;

    final static KeyStroke ksNE
            = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
    final static KeyStroke ksNA
            = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);

    // menu mnemonics
    final static int FILEMENU = KeyEvent.VK_F;
    final static int TAGMENU = KeyEvent.VK_C;
    final static int LINKMENU = KeyEvent.VK_L;
    final static int LINKARGMENU = KeyEvent.VK_K;
    final static int SETARGMENU = KeyEvent.VK_S;
    final static int NCMENU = KeyEvent.VK_N;
    final static int MODEMENU = KeyEvent.VK_M;
    final static int DPMENU = KeyEvent.VK_D;
    final static int HELPMENU = KeyEvent.VK_H;
    final static int OK_BUTTON = KeyEvent.VK_O;
    final static int CANCEL_BUTTON = KeyEvent.VK_C;
    
    // file menu keys
    final static KeyStroke ksCN
            = KeyStroke.getKeyStroke(KeyEvent.VK_N, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke NEWTASK = ksCN;
    
    final static KeyStroke ksCO
            = KeyStroke.getKeyStroke(KeyEvent.VK_O,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke OPENFILE = ksCO;

    final static KeyStroke ksCI
            = KeyStroke.getKeyStroke(KeyEvent.VK_I,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke ADDFILE = ksCI;

    final static KeyStroke ksCR
            = KeyStroke.getKeyStroke(KeyEvent.VK_R, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke SAVERTF = ksCR;

    final static KeyStroke CLOSEFILE = ksF4;

    final static KeyStroke ksCS
            = KeyStroke.getKeyStroke(KeyEvent.VK_S, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke SAVEXML = ksCS;
    
    // display menu keys
    final static KeyStroke ksCMinus
            = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke FONTSMALL = ksCMinus;
    
    final static KeyStroke ksCEquals
            = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke FONTBIG = ksCEquals;
    
    final static KeyStroke ksCPlus
            = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    
    final static KeyStroke SIDEPANEL = ksF9;
    
    // mode menu keys
    final static KeyStroke ksCE
            = KeyStroke.getKeyStroke(KeyEvent.VK_E, 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    final static KeyStroke NORMALMODE = ksCE;
    final static KeyStroke MSPANMODE = ksC1;
    final static KeyStroke ARGSMODE = ksC2;
    final static KeyStroke ADJUDMODE = ksC9;

    // help menu keys
    final static  KeyStroke ABOUT = ksF10;
    final static  KeyStroke WEB = ksF1;
    
    // switch between tabs
    final static KeyStroke ksCTab
            = KeyStroke.getKeyStroke("ctrl Tab");
    final static KeyStroke ksCShTab
            = KeyStroke.getKeyStroke("ctrl shift Tab");
    final static KeyStroke ksCPgup
            = KeyStroke.getKeyStroke("ctrl PgUp");
    final static KeyStroke ksCPgdn
            = KeyStroke.getKeyStroke("ctrl PgDown");
    final static KeyStroke NEXTTAB = ksCTab;
    final static KeyStroke PREVTAB = ksCShTab;
    final static KeyStroke TABUP = ksCPgup;
    final static KeyStroke TABDOWN = ksCPgdn;
}

