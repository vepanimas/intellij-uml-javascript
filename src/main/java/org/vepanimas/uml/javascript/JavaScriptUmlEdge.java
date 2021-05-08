package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramEdgeBase;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JavaScriptUmlEdge extends DiagramEdgeBase<PsiElement> {
    public JavaScriptUmlEdge(@NotNull DiagramNode<PsiElement> source,
                             @NotNull DiagramNode<PsiElement> target,
                             @NotNull DiagramRelationshipInfo relationship) {
        super(source, target, relationship);
    }
}
