package org.vepanimas.uml.javascript;

import com.intellij.diagram.AbstractDiagramNodeContentManager;
import com.intellij.diagram.DiagramCategory;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.uml.UmlIcons;
import com.intellij.uml.utils.DiagramBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptUmlNodeContentManager extends AbstractDiagramNodeContentManager {
    public static final @NotNull DiagramCategory FIELDS =
            new DiagramCategory(DiagramBundle.message("category.name.fields"), AllIcons.Nodes.Field);
    public static final @NotNull DiagramCategory CONSTRUCTORS =
            new DiagramCategory(DiagramBundle.message("category.name.constructors"), UmlIcons.Constructor);
    public static final @NotNull DiagramCategory METHODS =
            new DiagramCategory(DiagramBundle.message("category.name.methods"), AllIcons.Nodes.Method);
    public static final @NotNull DiagramCategory PROPERTIES =
            new DiagramCategory(DiagramBundle.message("category.name.properties"), AllIcons.Nodes.Property);

    private final static DiagramCategory @NotNull [] CATEGORIES = {FIELDS, CONSTRUCTORS, METHODS, PROPERTIES};

    @Override
    public DiagramCategory @NotNull [] getContentCategories() {
        return CATEGORIES;
    }

    @Override
    public boolean isInCategory(final @Nullable Object element, final @NotNull DiagramCategory category) {
        if (PROPERTIES.equals(category)) {
            return JavaScriptUmlUtils.isGetterOrSetter(element) || JavaScriptUmlUtils.isInterfaceProperty(element);
        } else if (CONSTRUCTORS.equals(category)) {
            return JavaScriptUmlUtils.isConstructor(element);
        } else if (METHODS.equals(category)) {
            return JavaScriptUmlUtils.isMethod(element);
        } else if (FIELDS.equals(category)) {
            return element instanceof JSField && !JavaScriptUmlUtils.isInterfaceProperty(element);
        }
        return false;
    }
}
