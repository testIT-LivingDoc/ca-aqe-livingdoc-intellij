package info.novatec.testit.livingdoc.intellij.gui.toolwindows.action;

import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import info.novatec.testit.livingdoc.intellij.domain.*;
import info.novatec.testit.livingdoc.intellij.gui.toolwindows.RepositoryViewUI;
import info.novatec.testit.livingdoc.intellij.gui.toolwindows.RepositoryViewUtils;
import info.novatec.testit.livingdoc.intellij.run.ConfigurationTypeLivingDoc;
import info.novatec.testit.livingdoc.intellij.run.RemoteRunConfiguration;
import info.novatec.testit.livingdoc.intellij.util.I18nSupport;
import info.novatec.testit.livingdoc.runner.Main;
import info.novatec.testit.livingdoc.server.domain.Repository;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * LivingDoc execution on selected node (specification).
 * See {@link #update(AnActionEvent)} for the display restrictions.
 *
 * @see AnAction
 * @see RemoteRunConfiguration
 */
public class ExecuteDocumentAction extends AnAction {

    private final RepositoryViewUI repositoryViewUI;
    private boolean debugMode = false;

    /**
     * Creates the action with its text, description and icon.
     *
     * @param repositoryViewUI {@link RepositoryViewUI} User interface fot Repository View.
     * @param isDebugMode      Kind of execution: <ul>
     *                         <li>true to activate debug mode</li>
     *                         <li>false otherwise. In this case, you will see the run configuration user interface.</li></ul>
     */
    public ExecuteDocumentAction(final RepositoryViewUI repositoryViewUI, final boolean isDebugMode) {

        super();

        this.repositoryViewUI = repositoryViewUI;
        this.debugMode = isDebugMode;

        String text;
        Icon icon;

        if (debugMode) {
            text = I18nSupport.getValue("repository.view.action.debug.tooltip");
            icon = AllIcons.Actions.StartDebugger;

        } else {
            text = I18nSupport.getValue("repository.view.action.execute.tooltip");
            icon = AllIcons.Actions.Execute;
        }

        Presentation presentation = getTemplatePresentation();
        presentation.setText(text);
        presentation.setDescription(text);
        presentation.setIcon(icon);
    }

    /**
     * Action handler. Only specification nodes will be executed.<br>
     * TODO NOTE: Basic functionality with single selection, desired multiple selection.
     *
     * @param actionEvent Carries information on the invocation place
     */
    @Override
    public void actionPerformed(AnActionEvent actionEvent) {

        DefaultMutableTreeNode[] nodes = repositoryViewUI.getRepositoryTree().getSelectedNodes(DefaultMutableTreeNode.class, null);

        Object userObject = nodes[0].getUserObject();

        if (((Node) userObject).getType() == NodeType.SPECIFICATION) {

            SpecificationNode specificationNode = (SpecificationNode) userObject;

            Project project = actionEvent.getProject();

            assert project != null;
            RunManager runManager = RunManager.getInstance(project);
            ConfigurationTypeLivingDoc livingDocConfigurationType = ConfigurationTypeLivingDoc.getInstance();

            RunnerAndConfigurationSettings runnerAndConfigurationSettings =
                    runManager.getConfigurationTemplate(livingDocConfigurationType.getConfigurationFactories()[0]);
            runnerAndConfigurationSettings.setName(specificationNode.getName());
            runnerAndConfigurationSettings.setTemporary(false);

            // True to active the "Run" ToolWindow
            runnerAndConfigurationSettings.setActivateToolWindowBeforeRun(false);

            // True to show the "run configuration UI" before launching LivingDoc
            runnerAndConfigurationSettings.setEditBeforeRun(true);

            RemoteRunConfiguration runConfiguration =
                    (RemoteRunConfiguration) runnerAndConfigurationSettings.getConfiguration();
            fillRunConfiguration(runConfiguration, specificationNode);

            Executor executor;
            if (debugMode) {
                runnerAndConfigurationSettings.setEditBeforeRun(true);
                executor = DefaultDebugExecutor.getDebugExecutorInstance();
            } else {
                executor = DefaultRunExecutor.getRunExecutorInstance();
            }

            ProgramRunnerUtil.executeConfiguration(project, runnerAndConfigurationSettings, executor);
        }
    }

    /**
     * This action will be enabled only for executable nodes
     *
     * @param actionEvent Carries information on the invocation place
     */
    @Override
    public void update(AnActionEvent actionEvent) {

        super.update(actionEvent);

        DefaultMutableTreeNode[] selectedNodes = repositoryViewUI.getRepositoryTree().getSelectedNodes(DefaultMutableTreeNode.class, null);

        RepositoryViewUtils.setEnabledForExecutableNode(selectedNodes, actionEvent.getPresentation());
    }

    private void fillRunConfiguration(RemoteRunConfiguration runConfiguration, final SpecificationNode specificationNode) {

        ModuleNode moduleNode = RepositoryViewUtils.getModuleNode(specificationNode);
        runConfiguration.getAllModules().stream().filter(
                module -> StringUtils.equals(module.getName(), moduleNode.getModuleName())).forEach(runConfiguration::setModule);

        RepositoryNode repositoryNode = RepositoryViewUtils.getRepositoryNode(specificationNode);
        Repository repository = repositoryNode.getRepository();

        runConfiguration.setRepositoryUID(repository.getUid());
        runConfiguration.setRepositoryURL(repository.getBaseTestUrl());
        runConfiguration.setSpecificationName(specificationNode.getName());
        runConfiguration.setRepositoryClass(repository.getType().getClassName());
        runConfiguration.setCurrentVersion(specificationNode.isUsingCurrentVersion());
        runConfiguration.setRepositoryName(repository.getName());

        runConfiguration.MAIN_CLASS_NAME = Main.class.getName();

        runConfiguration.setStatusLine(repositoryViewUI.getStatusLine());
    }
}