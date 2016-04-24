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

package edu.brandeis.cs.nlp.mae.agreement.view;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.agreement.MaeAgreementMain;
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.*;

/**
 * Created by krim on 4/14/2016.
 */
public class MaeAgreementGUI extends JFrame {

    JButton buttonOK;
    JButton buttonCancel;

    private MappedSet<String, String> tagsAndAtts;
    private List<AgreementTypeSelectPanel> agrTypeSelectPanels;
    private AttTypeSelectPanel attTypeSelectionPanel;

    private File datasetDir;
    private File taskScheme;

    private MaeAgreementMain calc;
    private MaeDriverI driver;

    public MaeAgreementGUI(String taskSchemeName) throws FileNotFoundException, MaeIOException, MaeDBException {
        super("MAE IAA Calculator");
        this.taskScheme =  new File(taskSchemeName);
        setupDriver();

        // currently only support extent tags
        // TODO: 2016-04-17 17:53:40EDT think of a way to handle link tags
        this.tagsAndAtts = driver.getTagTypesAndAttTypes();

        this.datasetDir = null;
        this.agrTypeSelectPanels = new LinkedList<>();
        this.initUI();
    }

    void setupDriver() throws MaeIOException, MaeDBException, FileNotFoundException {
        String dbFilename = String.format("mae-iaa-%d", System.currentTimeMillis());
        File dbFile;
        try {
            dbFile = File.createTempFile(dbFilename, ".sqlite");
        } catch (IOException e) {
            throw new MaeIOException("Could not generate DB file:", e);
        }
        driver = new LocalSqliteDriverImpl(dbFile.getAbsolutePath());
        driver.readTask(taskScheme);
    }

    private void initUI() {
        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel topPanel = prepareFileSelector();
        JPanel mainPanel = prepareMainPanel();
        JPanel bottomPanel = prepareButtons();

        contentPanel.add(topPanel, BorderLayout.PAGE_START);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.PAGE_END);

        setContentPane(contentPanel);
        setSize(new Dimension(800, 700));
        getRootPane().setDefaultButton(buttonCancel);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPanel.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private JPanel prepareFileSelector() {
        JPanel fileSelector =  new JPanel();
        fileSelector.setLayout(new BoxLayout(fileSelector, BoxLayout.Y_AXIS));
        fileSelector.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        String taskName;
        if (this.driver == null) {
            taskName = "No DTD selected";
        } else {
            try {
                taskName = driver.getTaskName();
            } catch (MaeDBException e) {
                taskName = "Error reading DTD name";
            }
        }

        JLabel taskTitle = new JLabel("Task: ");
        taskTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        final JLabel selectedTask = new JLabel(taskName);
        selectedTask.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

        JPanel taskNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        taskNamePanel.add(taskTitle);
        taskNamePanel.add(selectedTask);

        JButton taskChooser = new JButton("Load DTD");
        taskChooser.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                taskScheme = fileChooser.getSelectedFile();
                try {
                    if (driver != null) driver.destroy();
                    setupDriver();
                    selectedTask.setText(driver.getTaskName());
                    // TODO: 2016-04-17 18:18:16EDT refresh UI based on new driver
                } catch (MaeIOException | MaeDBException | FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        taskChooser.setToolTipText("Not supported yet");
        taskChooser.setEnabled(false);


        JLabel dirTitle = new JLabel("Annotation Path : ");
        dirTitle.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
        final JTextArea selectedDir = new JTextArea("No dataset selected");
        selectedDir.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        selectedDir.setEditable(false);
        selectedDir.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        JScrollPane selectedDirScroller = new JScrollPane(selectedDir);
        selectedDirScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel dataDirPanel = new JPanel();
        dataDirPanel.setLayout(new BoxLayout(dataDirPanel, BoxLayout.X_AXIS));
        dataDirPanel.add(dirTitle);
        dataDirPanel.add(selectedDirScroller);

        JButton dirChooser = new JButton("Choose Annotation Location");
        dirChooser.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                datasetDir = fileChooser.getSelectedFile();
                String selectedDirString = datasetDir.getAbsolutePath();
                selectedDir.setText(selectedDirString);
            }
        });

        JPanel taskFileButtonPanel = prepareRightAlignedButtonPanel(taskChooser);
        JPanel dataDirButtonPanel = prepareRightAlignedButtonPanel(dirChooser);

        fileSelector.add(taskNamePanel);
        fileSelector.add(taskFileButtonPanel);
        fileSelector.add(Box.createVerticalStrut(5));
        fileSelector.add(dataDirPanel);
        fileSelector.add(dataDirButtonPanel);
        fileSelector.add(Box.createVerticalStrut(5));
        fileSelector.add(new JSeparator(SwingConstants.HORIZONTAL));
        fileSelector.add(Box.createVerticalStrut(5));
        return fileSelector;
    }

    private JPanel prepareMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        JPanel leftPanel = prepareLeftPane();
        JPanel rightPanel = prepareRightPane();
        leftPanel.setMinimumSize(new Dimension(450, 300));
        leftPanel.setPreferredSize(new Dimension(450, 300));
        leftPanel.setMaximumSize(new Dimension(450, 2000));
        mainPanel.add(leftPanel);
        mainPanel.add(Box.createHorizontalStrut(12));
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(Box.createHorizontalStrut(2));
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(Box.createHorizontalStrut(12));
        mainPanel.add(rightPanel);
        return mainPanel;
    }

    private JPanel prepareLeftPane() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(prepareAgrTypePanels(), BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel prepareRightPane() {

        this.attTypeSelectionPanel = new AttTypeSelectPanel(tagsAndAtts);
        return this.attTypeSelectionPanel;
    }

    private JPanel prepareButtons() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonOK = new JButton("Continue");
        buttonOK.addActionListener(e -> onOk());

        buttonCancel = new JButton("Close");
        buttonCancel.addActionListener(e -> onCancel());
        buttons.add(buttonOK);
        buttons.add(buttonCancel);

        return buttons;
    }

    private JPanel prepareRightAlignedButtonPanel(JButton dirChooser) {
        JPanel dataDirButtonPanel = new JPanel();
        dataDirButtonPanel.setLayout(new BoxLayout(dataDirButtonPanel, BoxLayout.X_AXIS));
        dataDirButtonPanel.add(Box.createHorizontalGlue());
        dataDirButtonPanel.add(dirChooser);
        return dataDirButtonPanel;
    }

    public void onOk() {
        try {
            computeAgreement();
        } catch (IOException | MaeDBException | SAXException | MaeIOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
    }

    public void onCancel() {
        try {
            closeDriver();
            dispose();
        } catch (MaeDBException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        } finally {
            dispose();
        }
    }

    public File getDatasetDir() {
        return datasetDir;
    }

    public void closeDriver() throws MaeDBException {
        this.driver.destroy();
    }

    private JScrollPane prepareAgrTypePanels() {
        JPanel agrTypeList = new JPanel();
        agrTypeList.setLayout(new BoxLayout(agrTypeList, BoxLayout.Y_AXIS));
        agrTypeList.add(new JSeparator(SwingConstants.VERTICAL));

        for (String tagTypeName : tagsAndAtts.keyList()) {
            AgreementTypeSelectPanel tagTypePanel = new AgreementTypeSelectPanel(tagTypeName);
            agrTypeSelectPanels.add(tagTypePanel);
            agrTypeList.add(tagTypePanel);
            agrTypeList.add(Box.createVerticalStrut(8));
        }
        agrTypeList.add(Box.createVerticalGlue());
        JScrollPane agrTypeListScroller = new JScrollPane(agrTypeList);
        agrTypeListScroller.setBorder(BorderFactory.createEmptyBorder());

        return agrTypeListScroller;
    }


    public Map<String, Integer> getAgrTypeSelection() {
        Map<String, Integer> selected = new TreeMap<>();
        for (AgreementTypeSelectPanel selectPanel : agrTypeSelectPanels) {
            selected.put(selectPanel.getTagTypeName(), selectPanel.getSelectedAgrType());

        }
        return selected;
    }

    public void computeAgreement() throws IOException, MaeDBException, MaeIOException, SAXException {
        this.calc = new MaeAgreementMain(this.driver);
        if (datasetDir == null) {
            JOptionPane.showMessageDialog(null, "Choose dataset path first!");
        } else {
            calc.loadAnnotationFiles(datasetDir);
            Map<String, Integer> selectedAgrTypes = getAgrTypeSelection();
            List<MappedSet<String, String>> targets = new ArrayList<>();
            for (int i = 1; i < AGR_TYPES_STRINGS.size(); i++) {
                targets.add(new MappedSet<>());
            }
            for (String tagTypeName : selectedAgrTypes.keySet()) {
                targets.get(selectedAgrTypes.get(tagTypeName)).putCollection(
                        tagTypeName, this.attTypeSelectionPanel.getSelectedAttTypes(tagTypeName));
            }
            String formatted = calc.calcAllToString(targets);

            JOptionPane.showMessageDialog(null, new JTextArea(formatted), "Inter-Annotator Agreements", JOptionPane.PLAIN_MESSAGE);
        }
    }

}
