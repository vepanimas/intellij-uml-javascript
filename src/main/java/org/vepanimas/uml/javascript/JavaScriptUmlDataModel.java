package org.vepanimas.uml.javascript;

import com.intellij.diagram.*;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class JavaScriptUmlDataModel extends DiagramDataModel<PsiElement> {
    private final VirtualFile myEditorFile;
    private final DiagramPresentationModel myPresentationModel;
    private final SmartPointerManager mySmartPointerManager;
    private final ModificationTracker myModificationTracker = new SimpleModificationTracker();

    private final @Nullable SmartPsiElementPointer<PsiElement> myInitialElement;
    private final @NotNull Set<DiagramNode<PsiElement>> myNodes = new HashSet<>();
    private final @NotNull Set<DiagramEdge<PsiElement>> myEdges = new HashSet<>();
    private final @NotNull Set<DiagramEdge<PsiElement>> myDependencyEdges = new HashSet<>();

    public JavaScriptUmlDataModel(
            @NotNull Project project,
            @Nullable PsiElement psiElement,
            @Nullable VirtualFile file,
            @NotNull DiagramPresentationModel presentationModel
    ) {
        super(project, Objects.requireNonNull(DiagramProvider.findByID(JavaScriptUmlProvider.ID)));

        myEditorFile = file;
        myPresentationModel = presentationModel;
        mySmartPointerManager = SmartPointerManager.getInstance(getProject());
        myInitialElement = psiElement != null ? mySmartPointerManager.createSmartPsiElementPointer(psiElement) : null;

        if (psiElement != null) {
            addElement(psiElement);
        }
    }

    @Override
    public @NotNull ModificationTracker getModificationTracker() {
        return myModificationTracker;
    }

    @Override
    public @NotNull Collection<? extends DiagramNode<PsiElement>> getNodes() {
        return myNodes;
    }

    @Override
    public @NotNull String getNodeName(@NotNull DiagramNode<PsiElement> diagramNode) {
        final PsiElement element = diagramNode.getIdentifyingElement();
        if (element instanceof JSClass) {
            return JavaScriptUmlBundle.message("javascript.uml.node.name", ((JSClass) element).getName());
        }
        return "";
    }

    @Override
    public @Nullable DiagramNode<PsiElement> addElement(PsiElement element) {
        if (element instanceof JSClass) {
            return addClass((JSClass) element, true);
        }
        if (element instanceof JSFile) {
            return addFile((JSFile) element);
        }
        return null;
    }

    private @Nullable DiagramNode<PsiElement> addFile(@NotNull JSFile file) {
        List<DiagramNode<PsiElement>> classes = PsiTreeUtil.findChildrenOfType(file, JSClass.class)
                .stream().map(jsClass -> this.addClass(jsClass, false)).collect(Collectors.toList());
        return ContainerUtil.getFirstItem(classes);
    }

    private @NotNull DiagramNode<PsiElement> addClass(@NotNull JSClass element, boolean addParents) {
        JavaScriptUmlNode node = addNode(element);
        if (addParents) {
            addParents(element);
        }
        return node;
    }

    private void addParents(@NotNull JSClass element) {
        Queue<JSClass> queue = new ArrayDeque<>();
        Set<JSClass> visited = new HashSet<>();

        queue.add(element);
        while (!queue.isEmpty()) {
            JSClass jsClass = queue.poll();
            visited.add(jsClass);
            this.addNode(jsClass);

            JSClass[] supers = jsClass.getSupers();
            for (JSClass superClass : supers) {
                if (!visited.contains(superClass)) {
                    queue.add(superClass);
                }
            }
        }
    }

    private @NotNull JavaScriptUmlNode addNode(@NotNull PsiElement element) {
        JavaScriptUmlNode node = createNode(element);
        myNodes.add(node);
        return node;
    }

    @Override
    public @NotNull Collection<? extends DiagramEdge<PsiElement>> getEdges() {
        return myEdges;
    }

    @Override
    public void refreshDataModel() {

    }

    @Override
    public void dispose() {
    }

    private @NotNull JavaScriptUmlNode createNode(@NotNull PsiElement element) {
        return new JavaScriptUmlNode(element, getProvider());
    }
}
