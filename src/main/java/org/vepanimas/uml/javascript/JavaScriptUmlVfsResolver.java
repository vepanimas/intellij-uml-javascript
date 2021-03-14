package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlVfsResolver implements DiagramVfsResolver<PsiElement> {

    @Override
    public @Nullable String getQualifiedName(PsiElement element) {
        if (element instanceof JSClass) {
            VirtualFile file = element.getContainingFile().getVirtualFile();
            String name = ((JSClass) element).getName();
            if (file == null || name == null) return null;
            return String.join("#", file.getPath(), name, Integer.toString(element.getTextOffset()));
        }
        return null;
    }

    @Override
    public @Nullable PsiElement resolveElementByFQN(@NotNull String s, @NotNull Project project) {
        String[] parts = s.split("#");
        if (parts.length != 3) return null;
        String path = parts[0];
        String expectedName = parts[1];
        String offsetStr = parts[2];
        int offset = StringUtil.parseInt(offsetStr, -1);
        if (offset == -1) return null;
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file == null) return null;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return null;
        PsiElement element = psiFile.findElementAt(offset);
        JSClass classElement = PsiTreeUtil.getNonStrictParentOfType(element, JSClass.class);
        if (classElement == null) return null;
        return StringUtil.equals(classElement.getName(), expectedName) ? classElement : null;
    }
}
