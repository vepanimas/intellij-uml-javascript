package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.DiagramRelationships;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

public interface JavaScriptUmlRelationship extends DiagramRelationshipInfo {

    @NotNull
    String getType();

    @Nullable
    PsiElement getElement();

    String ONE_TO_ONE = "ONE_TO_ONE";
    String ONE_TO_MANY = "ONE_TO_MANY";
    String DEPENDENCY = "DEPENDENCY";
    String CREATE = "CREATE";

    DiagramRelationshipInfo INTERFACE_GENERALIZATION = DiagramRelationships.INTERFACE_GENERALIZATION;
    DiagramRelationshipInfo GENERALIZATION = DiagramRelationships.GENERALIZATION;
    DiagramRelationshipInfo REALIZATION = DiagramRelationships.REALIZATION;

    final class Factory {

        public static JavaScriptUmlRelationship oneToOne(String label, @NotNull PsiElement element) {
            return new Impl(ONE_TO_ONE, DiagramLineType.SOLID, label, "1", "1", 1, DiagramRelationships.getAngleArrow(), DIAMOND, element,
                    true);
        }

        public static JavaScriptUmlRelationship oneToMany(String label, @NotNull PsiElement element) {
            return new Impl(ONE_TO_MANY, DiagramLineType.SOLID, label, "*", "1", 1, DiagramRelationships.getAngleArrow(), DIAMOND, element,
                    true);
        }

        public static JavaScriptUmlRelationship dependency(@Nullable String label, @NotNull PsiElement element) {
            return new Impl(DEPENDENCY, DiagramLineType.DASHED, StringUtil.notNullize(label), null, null, 1,
                    DiagramRelationships.getAngleArrow(), null, element, label != null);
        }

        public static JavaScriptUmlRelationship create(@NotNull PsiElement element) {
            return new Impl(CREATE, DiagramLineType.DASHED, DiagramRelationships.CREATE.getLabel(), null, null, 1,
                    DiagramRelationships.getAngleArrow(), null, element, false);
        }

        private static class Impl extends DiagramRelationshipInfoAdapter implements JavaScriptUmlRelationship {

            private final String myType;
            private final Shape myStartArrow;
            private final Shape myEndArrow;
            private final boolean myAllowMultipleLinks;

            @Nullable
            private final SmartPsiElementPointer<PsiElement> myElementPointer;

            Impl(@NotNull final String type,
                 final DiagramLineType lineType,
                 @Nullable final String label,
                 @Nullable final String fromLabel,
                 @Nullable final String toLabel,
                 final int width,
                 final Shape startArrow,
                 final Shape endArrow,
                 @Nullable PsiElement element,
                 boolean allowMultipleLinks) {
                super(type, lineType, label, fromLabel, toLabel, width);
                myType = type;
                myStartArrow = startArrow;
                myEndArrow = endArrow;
                myAllowMultipleLinks = allowMultipleLinks;
                myElementPointer =
                        element != null ? SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element) : null;
            }

            @Override
            @Nullable
            public PsiElement getElement() {
                return myElementPointer != null ? myElementPointer.getElement() : null;
            }

            @NotNull
            @Override
            public String getType() {
                return myType;
            }

            @Override
            public Shape getStartArrow() {
                return myStartArrow;
            }

            @Override
            public Shape getEndArrow() {
                return myEndArrow;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                final Impl impl = (Impl) o;

                if (!Objects.equals(myType, impl.myType)) return false;
                if (myAllowMultipleLinks != impl.myAllowMultipleLinks) return false;

                if (myAllowMultipleLinks) {
                    PsiElement element = getElement();
                    return element != null ? element.equals(impl.getElement()) : impl.getElement() == null;
                }

                return true;
            }

            @Override
            public int hashCode() {
                int result = myType != null ? myType.hashCode() : 0;
                result = 31 * result + (myAllowMultipleLinks ? 1 : 0);
                if (myAllowMultipleLinks) {
                    PsiElement element = getElement();
                    result = 31 * result + (element != null ? element.hashCode() : 0);
                }
                return result;
            }
        }
    }
}
