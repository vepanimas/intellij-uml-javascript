package org.vepanimas.uml.javascript.dependencies;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.vepanimas.uml.javascript.JavaScriptUmlBundle;

public enum JavaScriptUmlDependenciesSettingsOption {

    ONE_TO_ONE("javascript.uml.dependencies.one.to.one"),
    ONE_TO_MANY("javascript.uml.dependencies.one.to.many"),
    USAGES("javascript.uml.dependencies.usages"),
    CYCLIC("javascript.uml.dependencies.cyclic"),
    CREATE("javascript.uml.dependencies.create");

    private final String myResourceBundleKey;

    JavaScriptUmlDependenciesSettingsOption(@PropertyKey(resourceBundle = JavaScriptUmlBundle.BUNDLE) final String resourceBundleKey) {
        myResourceBundleKey = resourceBundleKey;
    }

    public @NotNull @Nls String getDisplayName() {
        return JavaScriptUmlBundle.message(myResourceBundleKey);
    }
}