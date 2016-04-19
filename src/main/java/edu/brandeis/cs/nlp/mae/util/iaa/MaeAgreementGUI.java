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

package edu.brandeis.cs.nlp.mae.util.iaa;

import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by krim on 4/14/2016.
 */
public class MaeAgreementGUI extends JFrame {

    JButton buttonOK;
    JButton buttonCancel;

    private static int PARTIALMATCH_TYPE = 0;
    private static String PARTIALMATCH_STRING = "partial match";
    private static int FULLMATCH_TYPE = 1;
    private static String FULLMATCH_STRING = "full match";
    private static int CODEASSGNMENT_TYPE = 2;
    private static String CODEASSGNMENT_STRING = "code assignment";
    private static int IGNORE_TYPE = 3;
    private static String IGNORE_STRING = "ignore this";
    private String[] AGR_TYPES_GUIDE_STRINGS = new String[]
            {PARTIALMATCH_STRING,
                    FULLMATCH_STRING,
                    CODEASSGNMENT_STRING,
                    IGNORE_STRING
            };


    private MappedSet<String, String> tagsAndAtts;
    private List<TagTypePanel> agrTypeSelectionPanels;
    private AttTypePanel attTypeSelectionPanel;

    private File datasetDir;
    private File taskScheme;

    private MaeAgreementCalc calc;
    private MaeDriverI driver;

    public MaeAgreementGUI(String taskSchemeName) throws FileNotFoundException, MaeIOException, MaeDBException {
        super("MAE IAA Calculator");
//        super(new JFrame(), "MAE IAA Calculator", false);
        this.taskScheme =  new File(taskSchemeName);
        setupDriver();

        // currently only support extent tags
        // TODO: 2016-04-17 17:53:40EDT think of a way to handle link tags
        this.tagsAndAtts = driver.getTagTypesAndAttTypes();

        this.datasetDir = null;
        this.agrTypeSelectionPanels = new LinkedList<>();
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
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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

        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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
        taskChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser(".");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    taskScheme = fileChooser.getSelectedFile();
                    try {
                        if (driver != null) driver.destroy();
                        setupDriver();
                        selectedTask.setText(driver.getTaskName());
                        // TODO: 2016-04-17 18:18:16EDT refresh UI based on new driver
                    } catch (MaeIOException | MaeDBException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
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
        dirChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser(".");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    datasetDir = fileChooser.getSelectedFile();
                    String selectedDirString = datasetDir.getAbsolutePath();
                    selectedDir.setText(selectedDirString);
                }
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

        this.attTypeSelectionPanel = new AttTypePanel(tagsAndAtts);
        return this.attTypeSelectionPanel;
    }

    private JPanel prepareButtons() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonOK = new JButton("Continue");
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onOk();
            }
        });

        buttonCancel = new JButton("Close");
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
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
            e.printStackTrace();
        }
    }

    public void onCancel() {
        try {
            closeDriver();
        } catch (MaeDBException e) {
            e.printStackTrace();
        }
        dispose();
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
            TagTypePanel tagTypePanel = new TagTypePanel(tagTypeName);
            agrTypeSelectionPanels.add(tagTypePanel);
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
        for (TagTypePanel selectionPanel : agrTypeSelectionPanels) {
            selected.put(selectionPanel.getTagTypeName(), selectionPanel.getSelectedAgrType());

        }
        return selected;
    }

    public void computeAgreement() throws IOException, MaeDBException, MaeIOException, SAXException {
        this.calc = new MaeAgreementCalc(this.driver);
        if (datasetDir == null) {
            JOptionPane.showMessageDialog(null, "Choose dataset path first!");
        } else {
            calc.loadAnnotationFiles(datasetDir);
            Map<String, Integer> selectedAgrType = getAgrTypeSelection();
            MappedSet<String, String> alphaU = new MappedSet<>();
            MappedSet<String, String> fleissKappa = new MappedSet<>();
            for (String tagTypeName : selectedAgrType.keySet()) {
                if (selectedAgrType.get(tagTypeName) == PARTIALMATCH_TYPE) {
                    alphaU.putCollection(tagTypeName, this.attTypeSelectionPanel.getSelectedAttTypes(tagTypeName));
                } else if (selectedAgrType.get(tagTypeName) == CODEASSGNMENT_TYPE) {
                    fleissKappa.putCollection(tagTypeName, this.attTypeSelectionPanel.getSelectedAttTypes(tagTypeName));
                }
            }
            String formatted = "";
            formatted += calc.agreementToString(calc.computeAlphaU(alphaU), "Alpha_U");

            JOptionPane.showMessageDialog(null, new JTextArea(formatted), "Inter-Annotator Agreements", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private class TagTypePanel extends JPanel {
        private String tagTypeName;
        private int selectedAgrType;

        public TagTypePanel(String tagTypeName) {
            this.tagTypeName = tagTypeName;
            this.selectedAgrType = 0;
            this.initUI();
        }

        private void initUI() {
            this.setLayout(new GridLayout(1,2));

            JLabel tagTypeNameLabel = new JLabel(this.tagTypeName);
            tagTypeNameLabel.setHorizontalAlignment(JLabel.CENTER);
            add(tagTypeNameLabel);
            final JComboBox<String> selTypeCombo = new JComboBox<>();

            for (int i = 0; i < AGR_TYPES_GUIDE_STRINGS.length; i++) {
                selTypeCombo.addItem(AGR_TYPES_GUIDE_STRINGS[i]);
            }
            selTypeCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedAgrType = selTypeCombo.getSelectedIndex();
                }
            });
            selTypeCombo.setSelectedIndex(0);
            add(selTypeCombo);
            setMaximumSize(new Dimension(400, 32));
            setPreferredSize(new Dimension(400, 28));
        }

        public String getTagTypeName() {
            return tagTypeName;
        }

        public int getSelectedAgrType() {
            return selectedAgrType;
            // TODO: 2016-04-15 09:33:46EDT expand this for different agr types
        }
    }

    private class AttTypePanel extends JPanel {
        private MappedSet<String, String> tagsAndAtts;
        private Map<String, AttTypeList> listMap;

        public AttTypePanel(MappedSet<String, String> tagsAndAtts) {
            this.tagsAndAtts = tagsAndAtts;

            prepareAttLists();
            initUI();
        }

        void prepareAttLists() {
            listMap = new LinkedHashMap<>();
            listMap.put("-", new AttTypeList("-", new String[0]));

            for (String tagTypeName : tagsAndAtts.keySet()) {
                List<String> attTypeNamesList = tagsAndAtts.getAsList(tagTypeName);
                String[] attTypeNames = new String[attTypeNamesList.size()];
                attTypeNamesList.toArray(attTypeNames);
                listMap.put(tagTypeName, new AttTypeList(tagTypeName, attTypeNames));
            }
        }

        private void initUI() {

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(prepareGuideTextArea());

            final JComboBox<String> tagTypeCombo = new JComboBox<>();
            final JPanel listPanel = new JPanel(new CardLayout());
            listPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    BorderFactory.createLoweredBevelBorder()));

            for (String tagTypeName : listMap.keySet()) {
                tagTypeCombo.addItem(tagTypeName);
                AttTypeList attList = listMap.get(tagTypeName);

                JScrollPane listScroller = new JScrollPane(attList);
                listScroller.setBorder(BorderFactory.createEmptyBorder());
                listPanel.add(listScroller, tagTypeName);
            }


            tagTypeCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    ((CardLayout) listPanel.getLayout()).show(listPanel, (String) e.getItem());
                }
            });
            tagTypeCombo.setSelectedIndex(0);
            ((JLabel)tagTypeCombo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

            add(tagTypeCombo);
            add(listPanel);
            setBorder(BorderFactory.createEmptyBorder(0,0,12,12));


        }

        private JComponent prepareGuideTextArea() {
            JTextArea guideText = new JTextArea("Select tag type first and select att type to calculate attribute types in the list. Use ctrl/cmd and/or shift keys to select multiple items.");
            guideText.setLineWrap(true);
            guideText.setWrapStyleWord(true);
            guideText.setEditable(false);
            guideText.setMargin(new Insets(4,4,4,4));

            guideText.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
            guideText.setBackground(UIManager.getColor("Panel.background"));

            JScrollPane guideTextScroller = new JScrollPane(guideText);
            guideTextScroller.setBorder(BorderFactory.createEmptyBorder());

            return guideTextScroller;
        }

        public List<String> getSelectedAttTypes(String tagTypeName) {
            return this.listMap.get(tagTypeName).getSelectedAttTypes();
        }
    }

    private class AttTypeList extends JList<String> {
        private String tagTypeName;

        public AttTypeList(String tagTypeName, String[] attTypeNames) {
            super(attTypeNames);
            this.tagTypeName = tagTypeName;
            this.setSelectedIndices(SpanHandler.range(0, attTypeNames.length));
        }

        public String getTagTypeName() {
            return tagTypeName;
        }

        public List<String> getSelectedAttTypes() {
            return this.getSelectedValuesList();
        }
    }

}
