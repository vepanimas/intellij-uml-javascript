package org.vepanimas.uml.javascript.dependencies;

import com.intellij.diagram.settings.DiagramConfiguration;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.psi.ResolveState;
import org.jetbrains.annotations.NotNull;
import org.vepanimas.uml.javascript.JavaScriptUmlRelationship;

import java.util.ArrayList;
import java.util.Collection;

public class JavaScriptUmlDependenciesAnalyzer {
    public @NotNull Collection<JavaScriptUmlDependencyInfo> compute(@NotNull JSClass jsClass) {
        JavaScriptUmlDependenciesConfiguration config =
                JavaScriptUmlDependenciesConfiguration.fromUmlConfiguration(DiagramConfiguration.getInstance());
        DependencyVisitor visitor = new DependencyVisitor(jsClass, config);

        jsClass.processDeclarations((element, state) -> {
            element.accept(visitor);
            return true;
        }, ResolveState.initial(), jsClass, jsClass);

        return visitor.myDependencies;
    }

    private final static class DependencyVisitor extends JSElementVisitor {
        private final JSClass myClass;
        private final JavaScriptUmlDependenciesConfiguration myConfig;
        private final Collection<JavaScriptUmlDependencyInfo> myDependencies = new ArrayList<>();

        public DependencyVisitor(@NotNull JSClass jsClass,
                                 @NotNull JavaScriptUmlDependenciesConfiguration config) {
            myClass = jsClass;
            myConfig = config;
        }

        private void add(@NotNull JSClass target, @NotNull JavaScriptUmlRelationship relationship) {
            if ((relationship.getType().equals(JavaScriptUmlRelationship.ONE_TO_ONE) && !myConfig.isShowOneToOne())
                    || (relationship.getType().equals(JavaScriptUmlRelationship.ONE_TO_MANY) && !myConfig.isShowOneToMany())
                    || (relationship.getType().equals(JavaScriptUmlRelationship.DEPENDENCY) && !myConfig.isShowUsages())
                    || (relationship.getType().equals(JavaScriptUmlRelationship.CREATE) && !myConfig.isShowCreate())
                    || ((JSPsiImplUtils.isTheSameClass(myClass, target)) && !myConfig.isShowCyclic())) {
                return;
            }

            myDependencies.add(new JavaScriptUmlDependencyInfo(target, relationship));
        }
    }
}
