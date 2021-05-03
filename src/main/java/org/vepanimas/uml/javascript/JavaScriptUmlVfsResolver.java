package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.lang.javascript.psi.JSFile;
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

    public static final String SEPARATOR = ":";

    @Override
    public @Nullable String getQualifiedName(PsiElement element) {
        if (element == null) return null;

        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) return null;

        if (element instanceof JSClass) {
            String name = ((JSClass) element).getQualifiedName();
            if (name == null) return null;
            return String.join(SEPARATOR, virtualFile.getPath(), name, Integer.toString(element.getTextOffset()));
        }
        if (element instanceof JSFile) {
            return virtualFile.getPath();
        }
        return null;
    }

    @Override
    public @Nullable PsiElement resolveElementByFQN(@NotNull String s, @NotNull Project project) {
        String[] parts = s.split(SEPARATOR);
        if (parts.length == 3) {
            return resolveClass(project, parts[0], parts[1], parts[2]);
        }
        return resolveAsFile(s, project);
    }

    private @Nullable PsiElement resolveAsFile(@NotNull String path, @NotNull Project project) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file == null) return null;
        return PsiManager.getInstance(project).findFile(file);
    }

    private @Nullable PsiElement resolveClass(@NotNull Project project, String path, String expectedQualifiedName, String offsetStr) {
        int offset = StringUtil.parseInt(offsetStr, -1);
        if (offset == -1) return null;
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file == null) return null;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) return null;
        PsiElement element = psiFile.findElementAt(offset);
        JSClass classElement = PsiTreeUtil.getNonStrictParentOfType(element, JSClass.class);
        if (classElement == null) return null;
        return StringUtil.equals(classElement.getQualifiedName(), expectedQualifiedName) ? classElement : null;
    }
}
