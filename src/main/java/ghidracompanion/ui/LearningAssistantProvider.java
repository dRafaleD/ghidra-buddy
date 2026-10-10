package ghidracompanion.ui;

import docking.ActionContext;
import docking.ComponentProvider;
import docking.WindowPosition;
import docking.action.DockingAction;
import docking.action.ToolBarData;
import ghidra.app.services.GoToService;
import ghidra.framework.plugintool.Plugin;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.address.Address;
import ghidracompanion.model.CodeContext;
import ghidracompanion.service.WorkspaceManager;
import resources.Icons;

import javax.swing.*;

/**
 * Dockable ComponentProvider embedding the Learning Assistant into the Ghidra CodeBrowser.
 */
public class LearningAssistantProvider extends ComponentProvider {

    private final LearningPanel panel;
    private final WorkspaceManager workspaceManager;
    private final PluginTool pluginTool;

    public LearningAssistantProvider(Plugin plugin, String owner, WorkspaceManager workspaceManager) {
        super(plugin.getTool(), "Learning Assistant", owner);
        this.pluginTool = plugin.getTool();
        this.workspaceManager = workspaceManager;
        this.panel = new LearningPanel(workspaceManager);

        setTitle("Learning Assistant");
        setIcon(Icons.INFO_ICON);
        setDefaultWindowPosition(WindowPosition.RIGHT);

        setupNavigation();
        createActions();
        setVisible(true);
    }

    private void setupNavigation() {
        panel.setNavigationCallback(targetAddress -> {
            if (pluginTool != null && targetAddress != null) {
                GoToService goToService = pluginTool.getService(GoToService.class);
                if (goToService != null) {
                    goToService.goTo(targetAddress);
                }
            }
        });
    }

    public void close() {
        panel.dispose();
        removeFromTool();
    }

    private void createActions() {
        DockingAction toggleModeAction = new DockingAction("Switch Mode", getOwner()) {
            @Override
            public void actionPerformed(ActionContext context) {
                workspaceManager.toggleMode(LearningAssistantProvider.this);
            }
        };
        toggleModeAction.setToolBarData(new ToolBarData(Icons.REFRESH_ICON, null));
        toggleModeAction.setDescription("Switch between Guided and Full modes");
        toggleModeAction.markHelpUnnecessary();
        addLocalAction(toggleModeAction);
    }

    public void updateContext(CodeContext context) {
        panel.updateContext(context);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }
}
