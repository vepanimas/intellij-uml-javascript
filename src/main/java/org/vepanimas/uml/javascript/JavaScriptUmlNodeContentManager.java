package org.vepanimas.uml.javascript;

import com.intellij.diagram.AbstractDiagramNodeContentManager;
import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramCategory;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.psi.PsiElement;
import com.intellij.uml.UmlIcons;
import com.intellij.uml.utils.DiagramBundle;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlNodeContentManager extends AbstractDiagramNodeContentManager {
    public static final @NotNull DiagramCategory FIELDS =
            new DiagramCategory(DiagramBundle.messagePointer("category.name.fields"), AllIcons.Nodes.Field, true);
    public static final @NotNull DiagramCategory CONSTRUCTORS =
            new DiagramCategory(DiagramBundle.messagePointer("category.name.constructors"), UmlIcons.Constructor, true);
    public static final @NotNull DiagramCategory METHODS =
            new DiagramCategory(DiagramBundle.messagePointer("category.name.methods"), AllIcons.Nodes.Method, true);
    public static final @NotNull DiagramCategory PROPERTIES =
            new DiagramCategory(DiagramBundle.messagePointer("category.name.properties"), AllIcons.Nodes.Property, true);

    private final static DiagramCategory @NotNull [] CATEGORIES = {FIELDS, CONSTRUCTORS, METHODS, PROPERTIES};

    @Override
    public DiagramCategory @NotNull [] getContentCategories() {
        return CATEGORIES;
    }

    @Override
    public boolean isInCategory(@Nullable Object nodeElement,
                                @Nullable Object item,
                                @NotNull DiagramCategory category,
                                @Nullable DiagramBuilder builder) {
        PsiElement element = ObjectUtils.tryCast(item, PsiElement.class);
        if (element == null) {
            return false;
        }

        if (PROPERTIES.equals(category)) {
            return JavaScriptUmlUtils.isGetterOrSetter(element) || JavaScriptUmlUtils.isInterfaceOrObjectProperty(element);
        } else if (CONSTRUCTORS.equals(category)) {
            return JavaScriptUmlUtils.isConstructor(element);
        } else if (METHODS.equals(category)) {
            return JavaScriptUmlUtils.isMethod(element);
        } else if (FIELDS.equals(category)) {
            return element instanceof JSField && !JavaScriptUmlUtils.isInterfaceOrObjectProperty(element);
        }
        return false;
    }
}
