package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.settings.DiagramConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.EnumSet;

public enum JavaScriptUmlDependenciesSettingsOption {

    ONE_TO_ONE("javascript.uml.dependencies.one.to.one"),
    ONE_TO_MANY("javascript.uml.dependencies.one.to.many"),
    USAGES("javascript.uml.dependencies.usages"),
    SELF("javascript.uml.dependencies.self"),
    CREATE("javascript.uml.dependencies.create");

    private final String myResourceBundleKey;

    JavaScriptUmlDependenciesSettingsOption(@PropertyKey(resourceBundle = JavaScriptUmlBundle.BUNDLE) final String resourceBundleKey) {
        myResourceBundleKey = resourceBundleKey;
    }

    public @NotNull @Nls String getDisplayName() {
        return JavaScriptUmlBundle.message(myResourceBundleKey);
    }

    public static @NotNull EnumSet<JavaScriptUmlDependenciesSettingsOption> getEnabled() {
        EnumSet<JavaScriptUmlDependenciesSettingsOption> result = EnumSet.noneOf(JavaScriptUmlDependenciesSettingsOption.class);

        DiagramProvider<Object> provider = DiagramProvider.findByID(JavaScriptUmlProvider.ID);
        if (provider == null) {
            return result;
        }

        DiagramConfiguration configuration = DiagramConfiguration.getInstance();
        for (JavaScriptUmlDependenciesSettingsOption option : values()) {
            if (configuration.isEnabledByDefault(provider, option.getDisplayName())) {
                result.add(option);
            }
        }
        return result;
    }
}