package ghidracompanion.ui;

import ghidra.program.model.address.Address;
import ghidracompanion.model.CodeContext;
import ghidracompanion.model.GlossaryEntry;
import ghidracompanion.model.WalkthroughStep;
import ghidracompanion.service.GlossaryService;
import ghidracompanion.service.WalkthroughService;
import ghidracompanion.service.WorkspaceManager;
import ghidracompanion.service.WorkspaceManager.Mode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main interactive Learning Assistant panel embedded directly into Ghidra's CodeBrowser tool.
 */
public class LearningPanel extends JPanel {

    private final WorkspaceManager workspaceManager;
    private final WalkthroughService walkthroughService = new WalkthroughService();
    private final GlossaryService glossaryService = new GlossaryService();

    private final ContextCard contextCard = new ContextCard();

    private final JLabel modeLabel = new JLabel("Mode: Guided Mode");
    private final JButton modeToggleButton = new JButton("Switch Mode");

    // Checklist UI
    private final JPanel checklistContainer = new JPanel();
    private final JLabel checklistProgressLabel = new JLabel("Progress: 0 / 5");

    // Glossary UI
    private final DefaultListModel<GlossaryEntry> glossaryListModel = new DefaultListModel<>();
    private final JList<GlossaryEntry> glossaryList = new JList<>(glossaryListModel);
    private final JTextField glossarySearchField = new JTextField(15);
    private final JTextArea glossaryDetailArea = new JTextArea();
    private final JLabel glossaryTermTitle = new JLabel("Select a term");
    private final JLabel glossaryCategoryBadge = new JLabel("");

    private Consumer<Address> navigationCallback;
    private final Consumer<Mode> modeListener = this::updateModeLabel;
    private final DocumentListener glossarySearchListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) { filter(); }
        @Override
        public void removeUpdate(DocumentEvent e) { filter(); }
        @Override
        public void changedUpdate(DocumentEvent e) { filter(); }
        private void filter() {
            refreshGlossaryList(glossarySearchField.getText());
        }
    };

    public LearningPanel(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
        setLayout(new BorderLayout());
        buildUI();
    }

    public void setNavigationCallback(Consumer<Address> callback) {
        this.navigationCallback = callback;
        contextCard.setNavigationCallback(callback);
    }

    private void buildUI() {
        // Top Toolbar
        JPanel topToolbar = new JPanel(new BorderLayout(8, 4));
        topToolbar.setBorder(new EmptyBorder(6, 10, 6, 10));

        modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD, 12f));
        updateModeLabel(workspaceManager.getCurrentMode());

        modeToggleButton.setFont(modeToggleButton.getFont().deriveFont(11f));
        modeToggleButton.addActionListener(e -> {
            workspaceManager.toggleMode(null);
            updateModeLabel(workspaceManager.getCurrentMode());
        });

        topToolbar.add(modeLabel, BorderLayout.WEST);
        topToolbar.add(modeToggleButton, BorderLayout.EAST);
        add(topToolbar, BorderLayout.NORTH);

        // Main Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Context Inspector
        JScrollPane contextScrollPane = new JScrollPane(contextCard);
        contextScrollPane.setBorder(null);
        tabbedPane.addTab("Context Inspector", contextScrollPane);

        // Tab 2: Walkthrough Checklist
        JPanel checklistTab = buildChecklistTab();
        tabbedPane.addTab("Guided Workflow", checklistTab);

        // Tab 3: Glossary
        JPanel glossaryTab = buildGlossaryTab();
        tabbedPane.addTab("RE Glossary", glossaryTab);

        add(tabbedPane, BorderLayout.CENTER);

        // Wire workspace listener
        workspaceManager.setModeChangeListener(modeListener);
    }

    private JPanel buildChecklistTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        checklistProgressLabel.setFont(checklistProgressLabel.getFont().deriveFont(Font.BOLD, 12f));
        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(resetBtn.getFont().deriveFont(11f));
        resetBtn.addActionListener(e -> {
            walkthroughService.resetProgress();
            refreshChecklistItems();
        });

        header.add(checklistProgressLabel, BorderLayout.WEST);
        header.add(resetBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Checklist Items
        checklistContainer.setLayout(new BoxLayout(checklistContainer, BoxLayout.Y_AXIS));
        refreshChecklistItems();

        JScrollPane scrollPane = new JScrollPane(checklistContainer);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshChecklistItems() {
        checklistContainer.removeAll();
        List<WalkthroughStep> steps = walkthroughService.getSteps();

        for (WalkthroughStep step : steps) {
            JPanel stepCard = new JPanel(new BorderLayout(6, 4));
            stepCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(6, 4, 6, 4)
            ));

            JCheckBox checkBox = new JCheckBox(step.getTitle(), step.isCompleted());
            checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD, 12f));
            checkBox.addActionListener(e -> {
                walkthroughService.setStepCompleted(step.getStepNumber(), checkBox.isSelected());
                updateChecklistProgress();
            });

            JTextArea descArea = new JTextArea(step.getDescription() + "\n" + step.getTargetHint());
            descArea.setEditable(false);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setOpaque(false);
            descArea.setFont(descArea.getFont().deriveFont(11f));
            descArea.setForeground(new Color(80, 80, 80));

            stepCard.add(checkBox, BorderLayout.NORTH);
            stepCard.add(descArea, BorderLayout.CENTER);

            checklistContainer.add(stepCard);
        }

        updateChecklistProgress();
        checklistContainer.revalidate();
        checklistContainer.repaint();
    }

    private void updateChecklistProgress() {
        checklistProgressLabel.setText(String.format("Progress: %d / %d Completed",
            walkthroughService.getCompletedCount(), walkthroughService.getSteps().size()));
    }

    private JPanel buildGlossaryTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(4, 4));
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(glossarySearchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Populate Glossary List
        refreshGlossaryList("");

        glossarySearchField.getDocument().addDocumentListener(glossarySearchListener);

        // Detail View
        JPanel detailPanel = new JPanel(new BorderLayout(6, 6));
        detailPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel detailHeader = new JPanel(new BorderLayout());
        glossaryTermTitle.setFont(glossaryTermTitle.getFont().deriveFont(Font.BOLD, 13f));
        glossaryCategoryBadge.setFont(glossaryCategoryBadge.getFont().deriveFont(Font.ITALIC, 11f));
        glossaryCategoryBadge.setForeground(new Color(41, 128, 185));

        detailHeader.add(glossaryTermTitle, BorderLayout.WEST);
        detailHeader.add(glossaryCategoryBadge, BorderLayout.EAST);
        detailPanel.add(detailHeader, BorderLayout.NORTH);

        glossaryDetailArea.setEditable(false);
        glossaryDetailArea.setLineWrap(true);
        glossaryDetailArea.setWrapStyleWord(true);
        glossaryDetailArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        glossaryDetailArea.setMargin(new Insets(4, 4, 4, 4));
        detailPanel.add(new JScrollPane(glossaryDetailArea), BorderLayout.CENTER);

        glossaryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        glossaryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                GlossaryEntry selected = glossaryList.getSelectedValue();
                if (selected != null) {
                    glossaryTermTitle.setText(selected.getTerm());
                    glossaryCategoryBadge.setText(selected.getCategory());
                    glossaryDetailArea.setText(selected.getSummary() + "\n\n"
                        + "Details:\n" + selected.getDetailedDescription() + "\n\n"
                        + "Analysis Tip:\n" + selected.getBeginnerTip());
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(glossaryList), detailPanel);
        splitPane.setResizeWeight(0.4);
        panel.add(splitPane, BorderLayout.CENTER);

        if (!glossaryListModel.isEmpty()) {
            glossaryList.setSelectedIndex(0);
        }

        return panel;
    }

    private void refreshGlossaryList(String query) {
        glossaryListModel.clear();
        for (GlossaryEntry entry : glossaryService.search(query)) {
            glossaryListModel.addElement(entry);
        }
    }

    public void dispose() {
        workspaceManager.removeModeChangeListener(modeListener);
        glossarySearchField.getDocument().removeDocumentListener(glossarySearchListener);
    }

    public void updateContext(CodeContext context) {
        contextCard.updateContext(context);
    }

    private void updateModeLabel(Mode mode) {
        modeLabel.setText("Mode: " + mode.getTitle());
        modeToggleButton.setText(mode == Mode.BEGINNER ? "Switch to Full Mode" : "Switch to Guided Mode");
    }
}
