package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramEdgeCreationPolicy;
import com.intellij.diagram.DiagramNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JavaScriptUmlEdgeCreationPolicy extends DiagramEdgeCreationPolicy<PsiElement> {
    @Override
    public boolean acceptSource(@NotNull DiagramNode<PsiElement> diagramNode) {
        return true;
    }

    @Override
    public boolean acceptTarget(@NotNull DiagramNode<PsiElement> diagramNode) {
        return true;
    }
}
