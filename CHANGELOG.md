## v0.13.3.0
* added: preference menu item to set filename suffix to be used when saving files

## v0.13.2.1
* fixed: existing tag management were not showing in text context menu when all tags in selected text is same type
* fixed: assigning color was not working when creating a new tag

## v0.13.2.0
* modified: now context menu from text panel shows tag management directly if there's only one tag associated with selected text

## v0.13.1.0
* added: preference menu
* added: option to return to normal mode on every tag creation

## v0.13.0.0
* started new developing version
* goal: intergrating MAI

## v0.12.3.3
* fixed multi-span algorithm to remove any overlapping between spans

## v0.12.3.2
* fixed hotkey for 9th tag in creating tag menus to 9 (formerly 0)

## v0.12.3.1
* updateed comments and javadoc

## v0.12.3.0
* added: 'unsaved file' warning now appears only when there are unsaved changes in back-end DB
* added: put an asterisk in title when there is such a change
* added: tags with very long spans are now truncated when they are shown in context menus
* fixed: main menu hotkeys for creating tags caused unintended tag creation, replace hotkeys with mnemonics

## v0.12.2.3
* added: more colors to tag table and removed ambiguous gray color
* fixed: Main window title not properly shown after saving a file

## v0.12.2.2
* hotfix: tags were not deleted in DB when deleting tags from table popup
* hotfix: deleting tag in text popup menu was not functioning

## v0.12.2.1
* hotfix: fixed whitespace issue in loading a DTD file

## v0.12.2
* fixed: color toggle button was not properly shown in OSX
* fixed: right click was not working in Linux and OSX
* fixed: hotkey conflicts in main menu

## v0.12.1
* fixed: color toggle not working after using toggle from all_tab
* fixed: SQL insert error when inserting underspecified link tags
* fixed: triple click on text pane in arg_select mode caused crash
* fixed: DB was not updated when deleting link tags
* fixed: hotkey conflicts in set_as_arg menu
* minor code optimizations

## v0.12.0
* added: multi-argument select mode
* added: selecting multiple arguments in text pane
* added: a tab for all extent tags in bottom table. this can be useful when creating a link tag by selecting arguments from table
* added: now can toggle extent tag coloring
* added: toggle colors/styles from tabs in the bottom table
* added: lots of lots of keyboard shortcuts
* removed: toggle styling arguments of link tags in display menu
* fixed: link display toggle now properly works
* fixed: adding an argument to a link didn't update back-end DB

## v0.11.7
* fixed java version problem (was not working on java 6 and 7)
* fixed bugs in multi-argument selection in table

## v0.11.6
* added link creation windows for multi-argument selection
* added selecting multiple arguments using bottom table
* removed ctrl/cmd control in text pane to create links

## v0.11.5
* fixed an error text highlighting by double clicks on link tags in bottom table
* changed set of colors for text coloring
* added color indicator in tab title

## v0.11.4  
* re-wrote table context menu for adding an argument functionality with singular row  

## v0.11.3  
* added keyboard shortcuts for menu items  

## v0.11.2  
* re-wrote text pane context menu for adding an argument from text selection

## v0.11.1  
* added proto type UI for n-ary link creation 
* added creating link tag menu in top menu bar

## v0.11.0  
* back-end works for n-ary link support

## 0.10.1
* fixed java version problem (was not working on java 6 and 7)

## 0.10.0
* Added support for extent tags with multiple/discontinuous spans
* Added status bar interface for different notifications

## 0.9.6
* Fixes a problem with deleting table entries encountered when sorting the tables based on different columns
* Re-introduces scrolling the text to the appropriate location when a tag ID is double-clicked
