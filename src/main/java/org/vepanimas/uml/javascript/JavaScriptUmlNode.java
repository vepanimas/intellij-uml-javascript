package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramProvider;
import com.intellij.diagram.PsiDiagramNode;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JavaScriptUmlNode extends PsiDiagramNode<PsiElement> {
    public JavaScriptUmlNode(@NotNull PsiElement psiElement,
                             @NotNull DiagramProvider<PsiElement> provider) {
        super(psiElement, provider);
    }

    @Override
    public @Nullable @Nls String getTooltip() {
        PsiElement psiElement = getElement();
        if (psiElement instanceof JSClass) {
            return "<html><b>" + ((JSClass) psiElement).getName() + "</b></html>";
        }
        return JavaScriptUmlBundle.message("javascript.uml.unknown.node.tooltip");
    }

    @Override
    public @Nullable Icon getIcon() {
        return ReadAction.compute(super::getIcon);
    }
}
