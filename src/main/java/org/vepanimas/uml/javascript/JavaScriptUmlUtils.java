package org.vepanimas.uml.javascript;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public final class JavaScriptUmlUtils {
    public static @Nullable String getQualifiedName(@Nullable PsiElement element) {
        if (element == null) return null;

        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) return null;

        if (element instanceof JSClass) {
            String name = ((JSClass) element).getQualifiedName();
            if (name == null) return null;
            return String.join("#", virtualFile.getPath(), name);
        }
        if (element instanceof JSFile) {
            return virtualFile.getPath();
        }
        return null;
    }
}
