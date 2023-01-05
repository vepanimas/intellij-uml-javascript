package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramVfsResolver;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlVfsResolver implements DiagramVfsResolver<PsiElement> {

    private static final String SEPARATOR = "#";

    @Override
    public @Nullable String getQualifiedName(PsiElement element) {
        if (element == null || !element.isValid()) return null;

        VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
        if (virtualFile == null) return null;

        if (element instanceof JSClass) {
            String name = ((JSClass) element).getQualifiedName();
            if (name == null) return null;
            String path = FileUtil.toSystemIndependentName(virtualFile.getPath());
            return String.join(SEPARATOR, path, name);
        }
        if (element instanceof JSFile || element instanceof PsiDirectory) {
            return virtualFile.getPath();
        }
        return null;
    }

    @Override
    public @Nullable PsiElement resolveElementByFQN(@NotNull String s, @NotNull Project project) {
        String[] parts = s.split(SEPARATOR);
        if (parts.length == 2) {
            return resolveClass(project, parts[0], parts[1]);
        }
        return resolveAsFile(s, project);
    }

    private @Nullable PsiElement resolveAsFile(@NotNull String path, @NotNull Project project) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file == null) return null;
        return file.isDirectory() ?
                PsiDirectoryFactory.getInstance(project).createDirectory(file) :
                PsiManager.getInstance(project).findFile(file);
    }

    private @Nullable PsiElement resolveClass(@NotNull Project project, String path, String expectedQualifiedName) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file == null) return null;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (!(psiFile instanceof JSFile)) return null;
        PsiElement resolved = JSResolveUtil.findType(expectedQualifiedName, psiFile, true);
        if (resolved != null) {
            return resolved;
        }

        return ContainerUtil.getFirstItem(JSResolveUtil.findNamedElementsInScope(expectedQualifiedName, ((JSFile) psiFile)));
    }
}
