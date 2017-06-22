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

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.agreement.MaeAgreementMain;
import edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings;
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.ALL_METRIC_TYPE_STRINGS;

/**
 * Created by krim on 4/14/2016.
 */
public class MaeAgreementGUI extends JFrame {

    JButton buttonCancel;

    // Currently only the left panel (annotator selection panel) needs to be updated
    // dynamically after dataset location is selected. So we're keeping track of it
    // as a instance field.
    // Center and right panels should also be traceable when IAA calc is provided as
    // a stand-alone GUI with open-DTD functionality.
    JPanel leftPanel;
//    JPanel centerPanel;
//    JPanel rightPanel;

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
        this.calc = new MaeAgreementMain(this.driver);

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
        setSize(new Dimension(1000, 700));
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
        taskChooser.setToolTipText("Not supported yet. Please use MAE main window to load a task DTD");
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
        dirChooser.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser(this.taskScheme.getParentFile());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                datasetDir = fileChooser.getSelectedFile();
                String selectedDirString = datasetDir.getAbsolutePath();
                selectedDir.setText(selectedDirString);
                try {
                    calc.indexDataset(datasetDir);
                    BorderLayout layout = (BorderLayout) leftPanel.getLayout();
                    leftPanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));
                    leftPanel.add(prepareAnnotatorSelectionPanel(calc.getAnnotators()),
                            BorderLayout.CENTER);
                    leftPanel.revalidate();

                } catch (MaeIOException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(), MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);
                    e1.printStackTrace();
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
        leftPanel = prepareAnnotatorPanel(null);
        JPanel centerPanel = prepareTagsPane();
        JPanel rightPanel = prepareAttsPane();

        centerPanel.setMinimumSize(new Dimension(200, 300));
        centerPanel.setPreferredSize(new Dimension(200, 300));
        centerPanel.setMaximumSize(new Dimension(200, 2000));
        mainPanel.add(leftPanel);
        mainPanel.add(Box.createHorizontalStrut(12));
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(Box.createHorizontalStrut(2));
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(Box.createHorizontalStrut(12));
        centerPanel.setMinimumSize(new Dimension(600, 300));
        centerPanel.setPreferredSize(new Dimension(600, 300));
        centerPanel.setMaximumSize(new Dimension(600, 2000));
        mainPanel.add(centerPanel);
        mainPanel.add(Box.createHorizontalStrut(12));
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(Box.createHorizontalStrut(2));
        mainPanel.add(new JSeparator(SwingConstants.VERTICAL));
        mainPanel.add(Box.createHorizontalStrut(12));
        mainPanel.add(rightPanel);
        return mainPanel;
    }

    private JComponent preparePanelTitle(String title) {
        JLabel titleArea = new JLabel(title);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        titleArea.setFont(font);
        titleArea.setHorizontalAlignment(JLabel.CENTER);
        titleArea.setPreferredSize(new Dimension(300, 40));
        titleArea.setBackground(UIManager.getColor("Panel.background"));
        return titleArea;

    }

    private JPanel prepareAnnotatorPanel(List<String> annotatorIDs) {
        JPanel annotatorsPanel = new JPanel(new BorderLayout());
        annotatorsPanel.add(preparePanelTitle(MaeAgreementStrings.ANNOTATOR_CONFIG_PANEL_TITLE),
                BorderLayout.NORTH);
        annotatorsPanel.add(prepareAnnotatorSelectionPanel(annotatorIDs));
        return annotatorsPanel;
    }

    private JComponent prepareAnnotatorSelectionPanel(List<String> annotatorIDs) {
        if (annotatorIDs == null || annotatorIDs.size() == 0) {
            return new VerboseTextArea("once you have a dataset selected!");
        }
        JCheckBox[] annotatorCheckBoxes = new JCheckBox[annotatorIDs.size()];
        JPanel annotatorList = new JPanel();
        annotatorList.setLayout(new BoxLayout(annotatorList, BoxLayout.Y_AXIS));

        // add each annotator
        for (int i = 0; i < annotatorIDs.size(); i++) {
            String annotatorID = annotatorIDs.get(i);
            JCheckBox annotatorCheckBox = new JCheckBox(annotatorID);
            annotatorCheckBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    calc.ignoreAnnotator(annotatorID);
                } else {
                    calc.approveAnnotator(annotatorID);
                }
            });
            annotatorList.add(annotatorCheckBox);
            annotatorCheckBoxes[i] = annotatorCheckBox;
        }

        // add "all" button at the end
        JCheckBox allAnnotatorCheckBox = new JCheckBox("Include All");
        allAnnotatorCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                for (JCheckBox check : annotatorCheckBoxes) {
                    check.setEnabled(true);
                }
            } else {
                for (JCheckBox check : annotatorCheckBoxes) {
                    check.setSelected(true);
                    check.setEnabled(false);
                }
            }
        });
        allAnnotatorCheckBox.setSelected(true);
        annotatorList.add(allAnnotatorCheckBox);

        // TODO: 6/18/2017 provide pairwise IAA calculation
        JCheckBox pairwise = new JCheckBox("Pairwise comparison");
        pairwise.setEnabled(false);
        pairwise.setToolTipText("Under development");
//        annotatorList.add(pairwise);

        JScrollPane annotatorListScroller = new JScrollPane(annotatorList);
        annotatorListScroller.setBorder(BorderFactory.createEmptyBorder());
        return annotatorListScroller;
    }

    private JPanel prepareTagsPane() {
        JPanel tagsPanel = new JPanel(new BorderLayout());
        tagsPanel.add(preparePanelTitle(MaeAgreementStrings.SCOPE_CONFIG_PANEL_TITLE),
                BorderLayout.NORTH);
        tagsPanel.add(prepareAgrTypePanels(), BorderLayout.CENTER);
        return tagsPanel;
    }

    private JPanel prepareAttsPane() {

        this.attTypeSelectionPanel = new AttTypeSelectPanel(tagsAndAtts);
        JPanel attsPanel = new JPanel(new BorderLayout());
        attsPanel.add(preparePanelTitle(MaeAgreementStrings.ATTS_CONFIG_PANEL_TITLE),
                BorderLayout.NORTH);
        attsPanel.add(this.attTypeSelectionPanel, BorderLayout.CENTER);
        return attsPanel;
    }

    private JPanel prepareButtons() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton buttonHelp = new JButton(MaeStrings.MENU_HELP);
        buttonHelp.addActionListener(e -> openWebsite(MaeStrings.IAA_HELP_WEBPAGE));

        JButton buttonOK = new JButton("Continue");
        buttonOK.addActionListener(e -> onOk());

        buttonCancel = new JButton("Close");
        buttonCancel.addActionListener(e -> onCancel());

        buttons.add(buttonHelp);
        buttons.add(buttonOK);
        buttons.add(buttonCancel);

        return buttons;
    }

    private void openWebsite(String Url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(
                        new URI(Url));
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException ignored) {
            }
        }


    }

    private JPanel prepareRightAlignedButtonPanel(JButton dirChooser) {
        JPanel dataDirButtonPanel = new JPanel();
        dataDirButtonPanel.setLayout(new BoxLayout(dataDirButtonPanel, BoxLayout.X_AXIS));
        dataDirButtonPanel.add(Box.createHorizontalGlue());
        dataDirButtonPanel.add(dirChooser);
        return dataDirButtonPanel;
    }

    private void onOk() {
        try {
            computeAgreement();
        } catch (IOException | MaeException | SAXException | RuntimeException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
    }

    private void onCancel() {
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

    private void closeDriver() throws MaeDBException {
        this.driver.destroy();
    }

    private JScrollPane prepareAgrTypePanels() {
        JPanel agrTypeList = new JPanel();
        agrTypeList.setLayout(new BoxLayout(agrTypeList, BoxLayout.Y_AXIS));

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

    private void computeAgreement() throws IOException, MaeException, SAXException {
        if (datasetDir == null) {
            JOptionPane.showMessageDialog(null, "Choose dataset path first!");
        } else {
            calc.loadXmlFiles();

            Map<String, MappedSet<String, String>> global = new TreeMap<>();
            Map<String, MappedSet<String, String>> local = new TreeMap<>();

            for (String metric : ALL_METRIC_TYPE_STRINGS) {
                global.put(metric, new MappedSet<>());
                local.put(metric, new MappedSet<>());
            }

            for (AgreementTypeSelectPanel selectPanel : agrTypeSelectPanels) {
                String tagTypeName = selectPanel.getTagTypeName();
                if (!selectPanel.isIgnored()) {
                    if (selectPanel.isGlobalScope()) {
                        global.get(selectPanel.getSelectedMetric()).putCollection(
                                tagTypeName, this.attTypeSelectionPanel.getSelectedAttTypes(tagTypeName));
                    } else {
                        local.get(selectPanel.getSelectedMetric()).putCollection(
                                tagTypeName, this.attTypeSelectionPanel.getSelectedAttTypes(tagTypeName));
                    }
                }
            }
            String result = "";
            result += calc.calcGlobalAgreementToString(global);
            result += calc.calcLocalAgreementToString(local);

            Map<String, String> parseWarnings = calc.getParseWarnings();
            if (parseWarnings.size() > 0) {
                StringBuilder warnings = new StringBuilder();
                for (String fileName : parseWarnings.keySet()) {
                    warnings.append(String.format("%s: \n %s\n  ===\n\n", fileName, parseWarnings.get(fileName)));
                }
                JOptionPane.showMessageDialog(null, new JTextArea(warnings.toString()), "Some problems found in the dataset", JOptionPane.PLAIN_MESSAGE);

            }

            String[] resultButtons = new String[]{
                    "Close",
                    "Export to a file",
                    MaeStrings.MENU_HELP
            };
            JPanel resultPanel = new JPanel(new BorderLayout());
            resultPanel.add(new VerboseTextArea("Please make sure you understand differences between metrices before you use these numbers. See the MAE wiki for more details."), BorderLayout.NORTH);
            JScrollPane scrollableText = new JScrollPane(new JTextArea(result));
            scrollableText.setPreferredSize(new Dimension(400, 600));
            resultPanel.add(scrollableText);
            int export = JOptionPane.showOptionDialog(null,
                    resultPanel, "Inter-Annotator Agreements",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, resultButtons, resultButtons[0]);
            if (export == 1) {
                exportResult(result);
            } else if (export == resultButtons.length - 1) {
                openWebsite(MaeStrings.IAA_HELP_WEBPAGE);
            }
        }
    }

    private void exportResult(String result) throws MaeDBException, IOException {
        String timestamp = (new SimpleDateFormat("yyMMdd-HHmmss")).format(new Date());
        String filename = String.format("iaa-%s-%s.txt", driver.getTaskName(), timestamp);
        File exportFile = new File(filename);
        JFileChooser fileChooser = new JFileChooser(this.taskScheme.getParentFile());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(exportFile);
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            exportFile = fileChooser.getSelectedFile();
            String resultToFile = String.format("Task name: %s\nDTD: %s\nDataset: %s\n\n%s",
                    this.driver.getTaskName(), this.driver.getTaskFileName(), this.datasetDir, result);
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8");
            fw.write(resultToFile);
            fw.close();
        }
    }
}
