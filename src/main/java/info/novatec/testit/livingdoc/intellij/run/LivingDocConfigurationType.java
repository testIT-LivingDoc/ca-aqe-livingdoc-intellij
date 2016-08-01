package info.novatec.testit.livingdoc.intellij.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import info.novatec.testit.livingdoc.intellij.util.I18nSupport;
import info.novatec.testit.livingdoc.intellij.util.Icons;
import info.novatec.testit.livingdoc.intellij.util.PluginProperties;

/**
 * Implementation of the run configuration type for LivingDoc.<br>
 * <b>NOTE: This class is configured in plugin.xml</b><br>
 * The list of available configuration types is shown when a user opens:
 * <i>Run > Edit Configuration > Add New Configuration</i><br>
 * It’s possible that one {@link com.intellij.execution.configurations.ConfigurationType} has more than one
 * {@link ConfigurationFactory} so you can add other factories calling {{@link #addFactory(ConfigurationFactory)}
 *
 * @see RemoteConfigurationFactory
 */
public class LivingDocConfigurationType extends ConfigurationTypeBase {

    protected LivingDocConfigurationType() {

        super(PluginProperties.getValue("plugin.run.configuration.type"),
                I18nSupport.getValue("run.configuration.type.title"),
                I18nSupport.getValue("run.configuration.type.title"),
                Icons.LIVINGDOC);

        addFactory(new RemoteConfigurationFactory(this));
    }

    public static LivingDocConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(LivingDocConfigurationType.class);
    }
}
