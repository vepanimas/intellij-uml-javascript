package org.vepanimas.uml.javascript.dependencies;

import com.intellij.diagram.settings.DiagramConfiguration;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.lang.javascript.psi.types.JSResolvableType;
import com.intellij.lang.javascript.psi.types.JSResolvedTypeInfo;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private final static class DependencyVisitor extends JSRecursiveWalkingElementVisitor {
        private final JSClass myClass;
        private final JavaScriptUmlDependenciesConfiguration myConfig;
        private final Collection<JavaScriptUmlDependencyInfo> myDependencies = new ArrayList<>();

        public DependencyVisitor(@NotNull JSClass jsClass,
                                 @NotNull JavaScriptUmlDependenciesConfiguration config) {
            myClass = jsClass;
            myConfig = config;
        }

        @Override
        public void visitJSVariable(@NotNull JSVariable node) {
            if (node instanceof JSField) {
                JSType type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(node.getJSType());
                boolean isOneToMany = JSTypeUtils.isArrayLikeType(type);
                if (isOneToMany) {
                    type = JSTypeUtils.getIndexableComponentType(type);
                }
                if (type instanceof JSNamedType && type instanceof JSResolvableType) {
                    JSResolvedTypeInfo typeInfo = ((JSResolvableType) type).resolveType();
                    JSClass target = typeInfo.getDeclarationOfType(JSClass.class);
                    if (target != null) {
                        if (isOneToMany) {
                            add(target, JavaScriptUmlRelationship.Factory.oneToMany(node.getName(), target));
                        } else {
                            add(target, JavaScriptUmlRelationship.Factory.oneToOne(node.getName(), target));
                        }
                    }
                }
            }

            super.visitJSVariable(node);
        }

        @Override
        public void visitJSNewExpression(@NotNull JSNewExpression node) {
            JSClass target = resolveClass(node.getMethodExpression());
            if (target != null) {
                add(target, JavaScriptUmlRelationship.Factory.create(target));
            }

            super.visitJSNewExpression(node);
        }

        @Override
        public void visitJSReferenceExpression(@NotNull JSReferenceExpression node) {
            JSClass target = resolveClass(node);
            if (target != null) {
                add(target, JavaScriptUmlRelationship.Factory.dependency(null, target));
            }

            super.visitJSReferenceExpression(node);
        }

        private @Nullable JSClass resolveClass(@Nullable JSExpression expression) {
            if (!(expression instanceof JSReferenceExpression)) return null;

            PsiElement target = ((JSReferenceExpression) expression).resolve();
            if (target instanceof JSFunction && ((JSFunction) target).isConstructor()) {
                target = JSResolveUtil.findParent(target);
            }
            return ObjectUtils.tryCast(target, JSClass.class);
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
