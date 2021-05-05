package org.vepanimas.uml.javascript;

import com.intellij.diagram.*;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnum;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.CompositeModificationTracker;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class JavaScriptUmlDataModel extends DiagramDataModel<PsiElement> {
    private final VirtualFile myEditorFile;
    private final DiagramPresentationModel myPresentationModel;
    private final SmartPointerManager mySmartPointerManager;
    private final SimpleModificationTracker myModificationTracker;

    private final @Nullable SmartPsiElementPointer<PsiElement> myInitialElement;

    private final @NotNull Map<String, SmartPsiElementPointer<JSClass>> myClasses = new HashMap<>();

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
        myModificationTracker = new CompositeModificationTracker(PsiManager.getInstance(getProject()).getModificationTracker());

        if (psiElement != null) {
            addElement(psiElement, true);
        }
    }

    @Override
    public @NotNull ModificationTracker getModificationTracker() {
        return myModificationTracker;
    }

    @Override
    public @NotNull Collection<? extends DiagramNode<PsiElement>> getNodes() {
        return new ArrayList<>(myNodes);
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
        DiagramNode<PsiElement> diagramNode = addElement(element, false);
        if (diagramNode != null) {
            myModificationTracker.incModificationCount();
        }
        return diagramNode;
    }

    private @Nullable DiagramNode<PsiElement> addElement(@Nullable PsiElement element, boolean isInitialization) {
        if (hasElement(element)) return null;

        if (element instanceof JSClass) {
            return ContainerUtil.getFirstItem(addClass((JSClass) element, isInitialization));
        }
        if (element instanceof JSFile) {
            return ContainerUtil.getFirstItem(addFile((JSFile) element));
        }
        return null;
    }

    private @NotNull List<DiagramNode<PsiElement>> addFile(@NotNull JSFile file) {
        return PsiTreeUtil.findChildrenOfType(file, JSClass.class)
                .stream()
                .flatMap(jsClass -> addClass(jsClass, false).stream())
                .collect(Collectors.toList());
    }

    private @NotNull List<DiagramNode<PsiElement>> addClass(@NotNull JSClass element, boolean addParents) {
        if (hasElement(element)) return Collections.emptyList();

        Set<JSClass> addedClasses = new HashSet<>();

        addedClasses.add(element);
        if (addParents) {
            processClassParents(element, new CommonProcessors.CollectProcessor<>(addedClasses));
        }

        for (JSClass addedClass : addedClasses) {
            String fqn = getFqn(addedClass);
            myClasses.put(fqn, mySmartPointerManager.createSmartPsiElementPointer(addedClass));
        }

        return ContainerUtil.map(addedClasses, this::createNode);
    }

    @Override
    public void removeNode(@NotNull DiagramNode<PsiElement> node) {
        removeElement(node.getIdentifyingElement());
    }

    private void removeElement(@NotNull PsiElement element) {
        var node = findNode(element);
        if (node != null) {
            myNodes.remove(node);
            ContainerUtil.filter(myEdges, e -> e.getTarget().equals(node) || e.getSource().equals(node)).forEach(myEdges::remove);
            ContainerUtil.filter(myDependencyEdges, e -> e.getTarget().equals(node) || e.getSource().equals(node))
                    .forEach(myDependencyEdges::remove);
            myModificationTracker.incModificationCount();
        }

        if (element instanceof JSClass) removeClass((JSClass) element);
    }

    private void removeClass(@NotNull JSClass jsClass) {
        myClasses.remove(getFqn(jsClass));
    }

    private void processClassParents(@NotNull JSClass element, @NotNull Processor<JSClass> processor) {
        Queue<JSClass> queue = new ArrayDeque<>();
        Set<JSClass> visited = new HashSet<>();

        queue.add(element);
        while (!queue.isEmpty()) {
            JSClass jsClass = queue.poll();
            if (visited.add(jsClass)) {
                if (!processor.process(jsClass)) return;
            } else {
                continue;
            }

            if (ignoreParentsFor(jsClass)) {
                continue;
            }

            Collections.addAll(queue, jsClass.getSupers());
        }
    }

    private static boolean ignoreParentsFor(@NotNull JSClass jsClass) {
        return jsClass instanceof TypeScriptEnum;
    }

    @Override
    public @NotNull Collection<? extends DiagramEdge<PsiElement>> getEdges() {
        return myEdges;
    }

    @Override
    public boolean hasElement(@Nullable PsiElement element) {
        return findNode(element) != null;
    }

    @Override
    public void refreshDataModel() {
        clearNodesAndEdges();
        updateModel();
    }

    private void clearNodesAndEdges() {
        myNodes.clear();
        myEdges.clear();
        myDependencyEdges.clear();
    }

    private void updateModel() {
        Set<JSClass> classes = getAllClasses();
        for (JSClass jsClass : classes) {
            myNodes.add(createNode(jsClass));
        }
    }

    private @NotNull Set<JSClass> getAllClasses() {
        return getElementsFromMap(myClasses);
    }

    @NotNull
    private static <T extends JSClass> Set<T> getElementsFromMap(@NotNull Map<String, SmartPsiElementPointer<T>> classes) {
        return classes.values().stream()
                .map(SmartPsiElementPointer::getElement)
                .filter(el -> el != null && el.isValid())
                .collect(Collectors.toSet());
    }

    private @Nullable DiagramNode<PsiElement> findNode(@Nullable PsiElement element) {
        String fqn = getFqn(element);
        if (fqn == null) return null;

        return getNodes().stream()
                .filter(node -> fqn.equals(getFqn(node.getIdentifyingElement())))
                .findFirst().orElse(null);
    }

    private @Nullable String getFqn(@Nullable PsiElement element) {
        if (element == null || !element.isValid()) return null;
        return getProvider().getVfsResolver().getQualifiedName(element);
    }

    @Override
    public boolean isDependencyDiagramSupported() {
        return true;
    }

    @Override
    public boolean isPsiListener() {
        return true;
    }

    @Override
    public void dispose() {
    }

    private @NotNull JavaScriptUmlNode createNode(@NotNull PsiElement element) {
        return new JavaScriptUmlNode(element, getProvider());
    }
}
