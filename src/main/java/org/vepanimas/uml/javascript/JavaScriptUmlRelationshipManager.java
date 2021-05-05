package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramCategory;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipManager;
import com.intellij.psi.PsiElement;
import com.intellij.uml.UmlIcons;
import com.intellij.uml.utils.DiagramBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JavaScriptUmlRelationshipManager implements DiagramRelationshipManager<PsiElement> {
    private static final DiagramCategory[] CATEGORIES = {new DiagramCategory(DiagramBundle.message("category.name.dependencies"), UmlIcons.Dependencies)};

    @Override
    @Nullable
    public DiagramRelationshipInfo getDependencyInfo(PsiElement e1, PsiElement e2, DiagramCategory category) {
        return null;
    }

    @Override
    public DiagramCategory @NotNull [] getContentCategories() {
        return CATEGORIES;
    }
}
