package org.vepanimas.uml.javascript;

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlElementManager extends AbstractDiagramElementManager<PsiElement> {
    @Override
    public @Nullable PsiElement findInDataContext(@NotNull DataContext dataContext) {
        final PsiElement element = dataContext.getData(CommonDataKeys.PSI_ELEMENT);
        if (element != null) {
            return element;
        }
        return null;
    }

    @Override
    public boolean isAcceptableAsNode(@Nullable Object o) {
        return o instanceof JSClass;
    }

    @Override
    public @Nullable @Nls String getElementTitle(PsiElement element) {
        return null;
    }

    @Override
    public @Nullable @Nls String getNodeTooltip(PsiElement element) {
        return null;
    }
}
