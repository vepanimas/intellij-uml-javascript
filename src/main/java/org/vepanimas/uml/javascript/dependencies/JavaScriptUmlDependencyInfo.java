package org.vepanimas.uml.javascript.dependencies;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import org.jetbrains.annotations.NotNull;
import org.vepanimas.uml.javascript.JavaScriptUmlRelationship;

public final class JavaScriptUmlDependencyInfo {
    private final JSClass myTarget;
    private final JavaScriptUmlRelationship myRelationshipInfo;

    public JavaScriptUmlDependencyInfo(@NotNull JSClass target, @NotNull JavaScriptUmlRelationship relationshipInfo) {
        myTarget = target;
        myRelationshipInfo = relationshipInfo;
    }

    public @NotNull JSClass getTarget() {
        return myTarget;
    }

    public @NotNull JavaScriptUmlRelationship getRelationshipInfo() {
        return myRelationshipInfo;
    }
}
