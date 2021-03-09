package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlVfsResolver implements DiagramVfsResolver<PsiElement> {

    @Override
    public @Nullable String getQualifiedName(PsiElement element) {
        return null;
    }

    @Override
    public @Nullable PsiElement resolveElementByFQN(@NotNull String s, @NotNull Project project) {
        return null;
    }
}
