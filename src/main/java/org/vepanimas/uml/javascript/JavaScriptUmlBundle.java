package org.vepanimas.uml.javascript;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class JavaScriptUmlBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE = "messages.DiagramBundle";
    private static final JavaScriptUmlBundle INSTANCE = new JavaScriptUmlBundle();

    private JavaScriptUmlBundle() {
        super(BUNDLE);
    }

    @NotNull
    public static @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}
