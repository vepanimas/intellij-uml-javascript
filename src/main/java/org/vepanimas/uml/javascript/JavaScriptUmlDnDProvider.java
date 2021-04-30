package org.vepanimas.uml.javascript;

import com.intellij.diagram.extras.providers.DiagramDnDProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

public final class JavaScriptUmlDnDProvider implements DiagramDnDProvider<PsiElement> {
    @Override
    public boolean isAcceptedForDnD(Object o, Project project) {
        return JavaScriptUmlUtils.isAcceptableAsSource(o);
    }

    @Override
    public PsiElement[] wrapToModelObject(Object o, Project project) {
        if (JavaScriptUmlUtils.isAcceptableAsSource(o)) {
            return new PsiElement[]{((PsiElement) o)};
        }

        return PsiElement.EMPTY_ARRAY;
    }
}