package org.vepanimas.uml.javascript;

import com.intellij.diagram.*;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnum;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.CompositeModificationTracker;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vepanimas.uml.javascript.dependencies.JavaScriptUmlDependenciesAnalyzer;
import org.vepanimas.uml.javascript.dependencies.JavaScriptUmlDependencyInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaScriptUmlDataModel extends DiagramDataModel<PsiElement> {
    private final SmartPointerManager mySmartPointerManager;
    private final SimpleModificationTracker myModificationTracker;

    private final @Nullable SmartPsiElementPointer<PsiElement> myInitialElement;

    private final @NotNull Map<String, SmartPsiElementPointer<JSClass>> myClasses = new HashMap<>();

    private final @NotNull Set<DiagramNode<PsiElement>> myNodes = new HashSet<>();
    private final @NotNull Set<DiagramEdge<PsiElement>> myEdges = new HashSet<>();
    private final @NotNull Set<DiagramEdge<PsiElement>> myDependencyEdges = new HashSet<>();

    private final Object myLock = new Object();

    public JavaScriptUmlDataModel(@NotNull Project project, @Nullable PsiElement psiElement) {
        super(project, Objects.requireNonNull(DiagramProvider.findByID(JavaScriptUmlProvider.ID)));

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
        synchronized (myLock) {
            return new ArrayList<>(myNodes);
        }
    }

    @Override
    public @NotNull Collection<DiagramEdge<PsiElement>> getEdges() {
        synchronized (myLock) {
            if (myDependencyEdges.isEmpty()) {
                return new HashSet<>(myEdges);
            } else {
                final var allEdges = new HashSet<>(myEdges);
                allEdges.addAll(myDependencyEdges);
                return allEdges;
            }
        }
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
        synchronized (myLock) {
            DiagramNode<PsiElement> diagramNode = addElement(element, false);
            if (diagramNode != null) {
                myModificationTracker.incModificationCount();
            }
            return diagramNode;
        }
    }

    private @Nullable DiagramNode<PsiElement> addElement(@Nullable PsiElement element, boolean isInitialization) {
        if (hasElement(element)) {
            return null;
        }

        if (element instanceof JSClass) {
            return ContainerUtil.getFirstItem(addClass((JSClass) element, isInitialization));
        }
        if (element instanceof JSFile) {
            return ContainerUtil.getFirstItem(addFile((JSFile) element));
        }
        if (element instanceof PsiDirectory) {
            return ContainerUtil.getFirstItem(addDirectory(element));
        }
        return null;
    }

    private @NotNull List<DiagramNode<PsiElement>> addDirectory(@NotNull PsiElement element) {
        VirtualFile dir = ((PsiDirectory) element).getVirtualFile();
        Project project = element.getProject();
        List<DiagramNode<PsiElement>> nodes = new ArrayList<>();
        VfsUtil.iterateChildrenRecursively(
                dir,
                file -> file.isDirectory() || JavaScriptUmlUtils.isSupportedFileType(file),
                file -> {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (psiFile instanceof JSFile) {
                        nodes.addAll(addFile(((JSFile) psiFile)));
                    }
                    return true;
                }
        );
        return nodes;
    }

    private @NotNull List<DiagramNode<PsiElement>> addFile(@NotNull JSFile file) {
        return PsiTreeUtil.findChildrenOfType(file, JSClass.class)
                .stream()
                .flatMap(jsClass -> addClass(jsClass, false).stream())
                .collect(Collectors.toList());
    }

    private @NotNull List<DiagramNode<PsiElement>> addClass(@NotNull JSClass element, boolean addParents) {
        if (hasElement(element)) {
            return Collections.emptyList();
        }

        Set<JSClass> addedClasses = new HashSet<>();

        addedClasses.add(element);
        if (addParents) {
            processParents(element, new CommonProcessors.CollectProcessor<>(addedClasses));
        }

        for (JSClass addedClass : addedClasses) {
            String fqn = getFqn(addedClass);
            myClasses.put(fqn, mySmartPointerManager.createSmartPsiElementPointer(addedClass));
        }

        return ContainerUtil.map(addedClasses, this::createNode);
    }

    @Override
    public void removeNode(@NotNull DiagramNode<PsiElement> node) {
        synchronized (myLock) {
            removeElement(node.getIdentifyingElement());
        }
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

        if (element instanceof JSClass) {
            removeClass((JSClass) element);
        }
    }

    private void removeClass(@NotNull JSClass jsClass) {
        myClasses.remove(getFqn(jsClass));
    }

    private void addEdge(@NotNull DiagramNode<PsiElement> from,
                         @NotNull DiagramNode<PsiElement> to,
                         @NotNull DiagramRelationshipInfo relationship) {
        addEdge(from, to, relationship, myEdges);
    }

    private void addDependencyEdge(@NotNull DiagramNode<PsiElement> from,
                                   @NotNull DiagramNode<PsiElement> to,
                                   @NotNull DiagramRelationshipInfo relationship) {
        addEdge(from, to, relationship, myDependencyEdges);
    }

    private void addEdge(@NotNull DiagramNode<PsiElement> from,
                         @NotNull DiagramNode<PsiElement> to,
                         @NotNull DiagramRelationshipInfo relationship,
                         @NotNull Collection<DiagramEdge<PsiElement>> storage) {
        JavaScriptUmlEdge newEdge = new JavaScriptUmlEdge(from, to, relationship);

        if (Stream.concat(myEdges.stream(), myDependencyEdges.stream()).anyMatch(edge1 -> isEdgeIgnored(edge1, newEdge))) {
            return;
        }

        for (DiagramEdge<PsiElement> suppressedEdge : ContainerUtil.filter(myDependencyEdges, edge -> isEdgeIgnored(newEdge, edge))) {
            myDependencyEdges.remove(suppressedEdge);
        }

        for (DiagramEdge<PsiElement> edge : storage) {
            if (edge.getSource() == from && edge.getTarget() == to && relationship.equals(edge.getRelationship())) {
                return;
            }
        }

        storage.add(newEdge);
    }

    private boolean isEdgeIgnored(@NotNull DiagramEdge<PsiElement> subEdge, @NotNull DiagramEdge<PsiElement> candidate) {
        JavaScriptUmlRelationship candidateRelationship = ObjectUtils.tryCast(candidate.getRelationship(), JavaScriptUmlRelationship.class);
        if (candidateRelationship == null || !candidateRelationship.getType().equals(JavaScriptUmlRelationship.DEPENDENCY)) {
            return false;
        }

        boolean sameElements = Objects.equals(subEdge.getSource(), candidate.getSource())
                && Objects.equals(subEdge.getTarget(), candidate.getTarget());
        if (!sameElements) {
            return false;
        }

        JavaScriptUmlRelationship relationship = ObjectUtils.tryCast(subEdge.getRelationship(), JavaScriptUmlRelationship.class);
        if (relationship == null) {
            return false;
        }

        return relationship == JavaScriptUmlRelationship.GENERALIZATION
                || relationship == JavaScriptUmlRelationship.INTERFACE_GENERALIZATION
                || relationship == JavaScriptUmlRelationship.REALIZATION
                || relationship.getType().equals(JavaScriptUmlRelationship.CREATE);
    }

    private static boolean showParentsFor(@NotNull JSClass jsClass) {
        return !(jsClass instanceof TypeScriptEnum);
    }

    @Override
    public boolean hasElement(@Nullable PsiElement element) {
        synchronized (myLock) {
            return findNode(element) != null;
        }
    }

    @Override
    public void refreshDataModel() {
        synchronized (myLock) {
            clearNodesAndEdges();
            updateModel();
        }
    }

    private void clearNodesAndEdges() {
        myNodes.clear();
        myEdges.clear();
        myDependencyEdges.clear();
    }

    private void updateModel() {
        Set<JSClass> classes = getAllClasses();
        Set<JSClass> interfaces = new HashSet<>();

        for (JSClass jsClass : classes) {
            myNodes.add(createNode(jsClass));

            if (jsClass.isInterface()) {
                interfaces.add(jsClass);
            }
        }

        for (JSClass jsClass : classes) {
            DiagramNode<PsiElement> source = findNode(jsClass);
            if (source == null) {
                continue;
            }

            addClassGeneralizationEdges(jsClass, source, classes);
            addInterfaceGeneralizationEdges(jsClass, source, interfaces);
            addInterfaceRealizationEdges(jsClass, source, classes);
        }

        if (isShowDependencies()) {
            if (UndoManager.getInstance(getProject()).isUndoOrRedoInProgress()) {
                doShowDependenciesNow(null, classes);
            } else {
                showDependenciesLater(classes);
            }
        }
    }

    private void addClassGeneralizationEdges(@NotNull JSClass jsClass,
                                             @NotNull DiagramNode<PsiElement> source,
                                             @NotNull Set<JSClass> visibleElements) {
        if (jsClass.isInterface()) {
            return;
        }

        JSClass targetClass = findFirstReachableSuperClass(jsClass, visibleElements);
        DiagramNode<PsiElement> target = findNode(targetClass);
        if (target != null && source != target) {
            addEdge(source, target, JavaScriptUmlRelationship.GENERALIZATION);
        }
    }

    private void addInterfaceGeneralizationEdges(@NotNull JSClass jsClass,
                                                 @NotNull DiagramNode<PsiElement> source,
                                                 @NotNull Set<JSClass> visibleElements) {
        if (!jsClass.isInterface()) {
            return;
        }

        for (JSClass reachableInterface : findReachableInterfaces(jsClass, visibleElements)) {
            var target = findNode(reachableInterface);
            if (target != null && source != target) {
                addEdge(source, target, JavaScriptUmlRelationship.INTERFACE_GENERALIZATION);
            }
        }
    }

    private void addInterfaceRealizationEdges(@NotNull JSClass jsClass,
                                              @NotNull DiagramNode<PsiElement> source,
                                              @NotNull Set<JSClass> visibleElements) {
        if (jsClass.isInterface()) {
            return;
        }

        Queue<JSClass> interfaces = new ArrayDeque<>();
        ContainerUtil.addAll(interfaces, jsClass.getImplementedInterfaces());

        Processor<JSClass> processor = superClass -> {
            if (!visibleElements.contains(superClass)) {
                ContainerUtil.addAll(interfaces, superClass.getImplementedInterfaces());
                return true;
            }
            return false;
        };
        processParents(jsClass, processor, JSClass::getSuperClasses);

        Set<JSClass> visited = new HashSet<>();
        while (!interfaces.isEmpty()) {
            var jsInterface = interfaces.poll();
            if (!visited.add(jsInterface)) {
                continue;
            }

            DiagramNode<PsiElement> target = findNode(jsInterface);
            if (target != null) {
                if (source != target) {
                    addEdge(source, target, JavaScriptUmlRelationship.REALIZATION);
                }
            } else {
                ContainerUtil.addAll(interfaces, jsInterface.getSuperClasses());
            }
        }
    }

    private void showDependenciesLater(@NotNull Set<? extends JSClass> classes) {
        ApplicationManager.getApplication().invokeLater(() -> ProgressManager.getInstance().run(
                new Task.Modal(getProject(), JavaScriptUmlBundle.message("javascript.uml.calculating.dependencies"), true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        synchronized (myLock) {
                            doShowDependenciesNow(indicator, classes);
                            ApplicationManager.getApplication().invokeLater(() -> getBuilder().update(true, false));
                        }
                    }

                    @Override
                    public void onCancel() {
                        setShowDependencies(false);
                    }
                }));
    }

    private void doShowDependenciesNow(@Nullable ProgressIndicator indicator, @NotNull Set<? extends JSClass> classes) {
        if (indicator != null) {
            indicator.setIndeterminate(false);
        }
        Map<DiagramNode<PsiElement>, Collection<JavaScriptUmlDependencyInfo>> computedDependencies = new HashMap<>();
        int classIdx = 1;
        for (JSClass jsClass : classes) {
            if (indicator != null) {
                indicator.checkCanceled();
            }

            ReadAction.run(() -> {
                if (indicator != null) {
                    indicator.setText(JavaScriptUmlBundle.message("javascript.uml.analyzing", jsClass.getName()));
                }
                DiagramNode<PsiElement> sourceNode = findNode(jsClass);
                if (sourceNode != null) {
                    computedDependencies.put(sourceNode, new JavaScriptUmlDependenciesAnalyzer().compute(jsClass));
                }
            });

            classIdx++;
            if (indicator != null) {
                indicator.setFraction((double) classIdx / classes.size());
            }
        }

        ReadAction.run(() -> {
            for (var dependencyInfo : computedDependencies.entrySet()) {
                if (indicator != null) {
                    indicator.checkCanceled();
                }
                var source = dependencyInfo.getKey();
                for (var dependency : dependencyInfo.getValue()) {
                    var target = findNode(dependency.getTarget());
                    if (target != null) {
                        addDependencyEdge(source, target, dependency.getRelationshipInfo());
                    }
                }
            }
        });
    }

    private static void processParents(@NotNull JSClass element, @NotNull Processor<JSClass> processor) {
        processParents(element, processor, JSClass::getSupers, Collections.emptySet());
    }

    private static void processParents(@NotNull JSClass element,
                                       @NotNull Processor<JSClass> processor,
                                       @NotNull Function<JSClass, JSClass[]> getParents) {
        processParents(element, processor, getParents, Collections.emptySet());
    }

    private static void processParents(@NotNull JSClass element,
                                       @NotNull Processor<JSClass> processor,
                                       @NotNull Set<JSClass> stopAt) {
        processParents(element, processor, JSClass::getSupers, stopAt);
    }

    private static void processParents(@NotNull JSClass element,
                                       @NotNull Processor<JSClass> processor,
                                       @NotNull Function<JSClass, JSClass[]> getParents,
                                       @NotNull Set<JSClass> stopAt) {
        if (!showParentsFor(element)) {
            return;
        }

        Queue<JSClass> queue = new ArrayDeque<>();
        Set<JSClass> visited = new HashSet<>();

        visited.add(element);
        Collections.addAll(queue, getParents.apply(element));

        while (!queue.isEmpty()) {
            JSClass jsClass = queue.poll();
            if (!visited.add(jsClass)) {
                continue;
            }
            if (!processor.process(jsClass)) {
                return;
            }

            // prevent the tree walk-up for this subtree branch
            if (stopAt.contains(jsClass)) {
                continue;
            }
            if (showParentsFor(jsClass)) {
                Collections.addAll(queue, getParents.apply(jsClass));
            }
        }
    }

    private @Nullable JSClass findFirstReachableSuperClass(@NotNull JSClass jsClass, @NotNull Set<JSClass> stopAt) {
        CommonProcessors.FindProcessor<JSClass> processor = new CommonProcessors.FindProcessor<>() {
            @Override
            protected boolean accept(JSClass jsClass) {
                return stopAt.contains(jsClass);
            }
        };
        processParents(jsClass, processor, JSClass::getSuperClasses);
        return processor.getFoundValue();
    }

    private @NotNull Collection<JSClass> findReachableInterfaces(@NotNull JSClass jsClass, @NotNull Set<JSClass> stopAt) {
        CommonProcessors.CollectProcessor<JSClass> processor = new CommonProcessors.CollectProcessor<>() {
            @Override
            protected boolean accept(JSClass jsClass) {
                return stopAt.contains(jsClass);
            }
        };
        processParents(jsClass, processor, stopAt);
        return processor.getResults();
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
        if (fqn == null) {
            return null;
        }

        return getNodes().stream()
                .filter(node -> fqn.equals(getFqn(node.getIdentifyingElement())))
                .findFirst().orElse(null);
    }

    private @Nullable String getFqn(@Nullable PsiElement element) {
        if (element == null || !element.isValid()) {
            return null;
        }
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
