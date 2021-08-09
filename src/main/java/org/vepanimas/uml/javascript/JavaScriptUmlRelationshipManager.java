package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramCategory;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public final class JavaScriptUmlRelationshipManager implements DiagramRelationshipManager<PsiElement> {
    @Override
    @Nullable
    public DiagramRelationshipInfo getDependencyInfo(PsiElement e1, PsiElement e2, DiagramCategory category) {
        return null;
    }
}
