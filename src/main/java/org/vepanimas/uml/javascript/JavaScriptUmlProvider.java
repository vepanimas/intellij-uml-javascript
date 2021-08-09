package org.vepanimas.uml.javascript;

import com.intellij.diagram.*;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlProvider extends DiagramProvider<PsiElement> {
    public static final String ID = "JavaScriptClasses";

    private final JavaScriptUmlElementManager myElementManager = new JavaScriptUmlElementManager();
    private final JavaScriptUmlVfsResolver myVfsResolver = new JavaScriptUmlVfsResolver();
    private final JavaScriptUmlVisibilityManager myVisibilityManager = new JavaScriptUmlVisibilityManager();
    private final JavaScriptUmlExtras myExtras = new JavaScriptUmlExtras();
    private final JavaScriptUmlEdgeCreationPolicy myEdgeCreationPolicy = new JavaScriptUmlEdgeCreationPolicy();
    private final JavaScriptUmlRelationshipManager myRelationshipManager = new JavaScriptUmlRelationshipManager();

    public JavaScriptUmlProvider() {
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
        return new JavaScriptUmlDataModel(project, element);
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
        return myVisibilityManager;
    }

    @Override
    public @NotNull DiagramNodeContentManager createNodeContentManager() {
        return new JavaScriptUmlNodeContentManager();
    }

    @Override
    public @NotNull @Nls String getActionName(boolean isPopup) {
        return super.getActionName(isPopup);
    }

    @Override
    public @NotNull DiagramExtras<PsiElement> getExtras() {
        return myExtras;
    }

    @Override
    public @NotNull DiagramRelationshipManager<PsiElement> getRelationshipManager() {
        return myRelationshipManager;
    }
}
