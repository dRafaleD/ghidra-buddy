package ghidracompanion.ui;

import ghidra.program.model.address.Address;
import ghidracompanion.model.CodeContext;
import ghidracompanion.model.CodeContext.ActionSuggestion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Modern Swing card displaying what the user is seeing, why it matters,
 * and what to inspect next with 1-click action buttons.
 */
public class ContextCard extends JPanel {

    private final JLabel titleLabel = new JLabel("No Selection");
    private final JLabel typeBadge = new JLabel("General Analysis");
    private final JLabel addressBadge = new JLabel("-");

    private final JTextArea whatItIsArea = createReadOnlyTextArea();
    private final JTextArea whyItMattersArea = createReadOnlyTextArea();
    private final JTextArea whatToInspectArea = createReadOnlyTextArea();

    private final JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));

    private Consumer<Address> navigationCallback;

    public ContextCard() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    public void setNavigationCallback(Consumer<Address> callback) {
        this.navigationCallback = callback;
    }

    private void buildUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(8, 4));
        headerPanel.setOpaque(false);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        typeBadge.setFont(typeBadge.getFont().deriveFont(Font.BOLD, 11f));
        typeBadge.setForeground(new Color(41, 128, 185));

        addressBadge.setFont(addressBadge.getFont().deriveFont(Font.PLAIN, 12f));
        addressBadge.setForeground(Color.GRAY);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        badges.setOpaque(false);
        badges.add(typeBadge);
        badges.add(new JLabel("•"));
        badges.add(addressBadge);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(badges, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Body Content
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);

        bodyPanel.add(createSectionPanel("What It Is", whatItIsArea));
        bodyPanel.add(Box.createVerticalStrut(8));
        bodyPanel.add(createSectionPanel("Why It Matters", whyItMattersArea));
        bodyPanel.add(Box.createVerticalStrut(8));
        bodyPanel.add(createSectionPanel("Next Inspection Step", whatToInspectArea));
        bodyPanel.add(Box.createVerticalStrut(10));

        // Actions Section
        actionsPanel.setOpaque(false);
        bodyPanel.add(actionsPanel);

        add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String header, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setOpaque(false);

        JLabel label = new JLabel(header);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(new Color(52, 73, 94));

        panel.add(label, BorderLayout.NORTH);
        panel.add(textArea, BorderLayout.CENTER);
        return panel;
    }

    private JTextArea createReadOnlyTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(2, 4, 2, 4));
        return area;
    }

    public void updateContext(CodeContext context) {
        if (context == null) {
            context = CodeContext.empty(null);
        }

        titleLabel.setText(context.getItemName().isEmpty() ? "Location Selected" : context.getItemName());
        typeBadge.setText(context.getContextType().getLabel());
        addressBadge.setText(context.getAddress() != null ? context.getAddress().toString() : "-");

        whatItIsArea.setText(context.getWhatItIs());
        whyItMattersArea.setText(context.getWhyItMatters());
        whatToInspectArea.setText(context.getWhatToInspectNext());

        actionsPanel.removeAll();
        for (ActionSuggestion suggestion : context.getSuggestions()) {
            JButton btn = new JButton(suggestion.getLabel());
            btn.setFont(btn.getFont().deriveFont(11f));
            btn.addActionListener(e -> {
                if (navigationCallback != null && suggestion.getTargetAddress() != null) {
                    navigationCallback.accept(suggestion.getTargetAddress());
                }
            });
            actionsPanel.add(btn);
        }

        actionsPanel.revalidate();
        actionsPanel.repaint();
        revalidate();
        repaint();
    }
}
