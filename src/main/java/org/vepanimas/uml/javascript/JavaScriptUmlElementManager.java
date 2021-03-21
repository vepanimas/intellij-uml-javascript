package org.vepanimas.uml.javascript;

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlElementManager extends AbstractDiagramElementManager<PsiElement> {
    @Override
    public @Nullable PsiElement findInDataContext(@NotNull DataContext dataContext) {
        PsiFile psiFile = dataContext.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof JSFile)) return null;
        if (!DialectDetector.isTypeScript(psiFile) && !DialectDetector.isJavaScript(psiFile)) {
            return null;
        }
        PsiElement element = dataContext.getData(CommonDataKeys.PSI_ELEMENT);
        if (element instanceof JSClass) {
            return element;
        } else {
            return psiFile;
        }
    }

    @Override
    public boolean canBeBuiltFrom(@Nullable Object element) {
        return isAcceptableAsNode(element) || element instanceof JSFile;
    }

    @Override
    public boolean isAcceptableAsNode(@Nullable Object o) {
        return o instanceof JSClass;
    }

    @Override
    public @Nullable @Nls String getElementTitle(PsiElement element) {
        if (element instanceof JSClass) {
            return ((JSClass) element).getQualifiedName();
        }
        if (element instanceof JSFile) {
            VirtualFile virtualFile = ((JSFile) element).getVirtualFile();
            if (virtualFile != null) {
                return virtualFile.getPresentableName();
            }
        }
        return null;
    }

    @Override
    public @Nullable @Nls String getNodeTooltip(PsiElement element) {
        if (element instanceof JSClass) {
            return ((JSClass) element).getQualifiedName();
        }
        return null;
    }
}
