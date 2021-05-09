package org.vepanimas.uml.javascript.dependencies;

import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.settings.DiagramConfigElement;
import com.intellij.diagram.settings.DiagramConfigGroup;
import com.intellij.diagram.settings.DiagramConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.vepanimas.uml.javascript.JavaScriptUmlExtras;
import org.vepanimas.uml.javascript.JavaScriptUmlProvider;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class JavaScriptUmlDependenciesConfiguration {
    private final boolean myShowOneToOne;
    private final boolean myShowOneToMany;
    private final boolean myShowUsages;
    private final boolean myShowCyclic;
    private final boolean myShowCreate;

    private JavaScriptUmlDependenciesConfiguration(
            boolean showOneToOne,
            boolean showOneToMany,
            boolean showUsages,
            boolean showCyclic,
            boolean showCreate
    ) {
        myShowOneToOne = showOneToOne;
        myShowOneToMany = showOneToMany;
        myShowUsages = showUsages;
        myShowCyclic = showCyclic;
        myShowCreate = showCreate;
    }

    public boolean isShowOneToOne() {
        return myShowOneToOne;
    }

    public boolean isShowOneToMany() {
        return myShowOneToMany;
    }

    public boolean isShowUsages() {
        return myShowUsages;
    }

    public boolean isShowCyclic() {
        return myShowCyclic;
    }

    public boolean isShowCreate() {
        return myShowCreate;
    }

    public static @NotNull JavaScriptUmlDependenciesConfiguration fromUmlConfiguration(@NotNull DiagramConfiguration diagramConfiguration) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return new JavaScriptUmlDependenciesConfiguration(true, true, true, true, true);
        }

        DiagramProvider<Object> provider = Objects.requireNonNull(DiagramProvider.findByID(JavaScriptUmlProvider.ID));
        DiagramConfigGroup[] diagramSettings = provider.getExtras().getAdditionalDiagramSettings();
        String configGroupName = JavaScriptUmlExtras.getConfigGroupName();
        DiagramConfigGroup dependenciesSettingsGroup = Objects.requireNonNull(
                ContainerUtil.find(diagramSettings, group -> group.getName().equals(configGroupName)));

        Map<String, Boolean> optionToIsEnabled = dependenciesSettingsGroup.getElements().stream()
                .filter(it -> it.getType() == DiagramConfigElement.Type.CHECKBOX)
                .collect(Collectors.toMap(DiagramConfigElement::getName, element -> diagramConfiguration.isEnabledByDefault(provider, element.getName())));

        return new JavaScriptUmlDependenciesConfiguration(
                optionToIsEnabled.get(JavaScriptUmlDependenciesSettingsOption.ONE_TO_ONE.getDisplayName()),
                optionToIsEnabled.get(JavaScriptUmlDependenciesSettingsOption.ONE_TO_MANY.getDisplayName()),
                optionToIsEnabled.get(JavaScriptUmlDependenciesSettingsOption.USAGES.getDisplayName()),
                optionToIsEnabled.get(JavaScriptUmlDependenciesSettingsOption.CYCLIC.getDisplayName()),
                optionToIsEnabled.get(JavaScriptUmlDependenciesSettingsOption.CREATE.getDisplayName())
        );
    }
}
