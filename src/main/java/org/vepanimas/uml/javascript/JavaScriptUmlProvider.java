package org.vepanimas.uml.javascript;

import com.intellij.diagram.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlProvider extends DiagramProvider<PsiElement> {
    public static final String ID = "JavaScriptClasses";

    private final JavaScriptUmlElementManager myElementManager;
    private final JavaScriptUmlVfsResolver myVfsResolver;

    public JavaScriptUmlProvider() {
        myElementManager = new JavaScriptUmlElementManager();
        myVfsResolver = new JavaScriptUmlVfsResolver();

        myElementManager.setUmlProvider(this);
    }

    @Override
    @Pattern("[a-zA-Z0-9_-]*")
    public @NotNull String getID() {
        return ID;
    }

    @Override
    public @NotNull String getPresentableName() {
        return JavaScriptUmlBundle.message("javascript.uml.presentable.name");
    }

    @Override
    public @NotNull DiagramDataModel<PsiElement> createDataModel(@NotNull Project project,
                                                                 @Nullable PsiElement element,
                                                                 @Nullable VirtualFile virtualFile,
                                                                 @NotNull DiagramPresentationModel diagramPresentationModel) {
        return new JavaScriptUmlDataModel(project, element, virtualFile, diagramPresentationModel);
    }

    @Override
    public @NotNull DiagramElementManager<PsiElement> getElementManager() {
        return myElementManager;
    }

    @Override
    public @NotNull DiagramVfsResolver<PsiElement> getVfsResolver() {
        return myVfsResolver;
    }

    @Override
    public @NotNull DiagramVisibilityManager createVisibilityManager() {
        return EmptyDiagramVisibilityManager.INSTANCE;
    }

    @Override
    public @NotNull DiagramRelationshipManager<PsiElement> getRelationshipManager() {
        //noinspection unchecked
        return (DiagramRelationshipManager<PsiElement>) DiagramRelationshipManager.NO_RELATIONSHIP_MANAGER;
    }

    @Override
    public @NotNull DiagramNodeContentManager createNodeContentManager() {
        return DiagramNodeContentManager.NO_CATEGORIES;
    }
}
