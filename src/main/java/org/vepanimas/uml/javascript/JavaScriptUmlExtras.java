package org.vepanimas.uml.javascript;

import com.intellij.diagram.DiagramElementsProvider;
import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.extras.providers.DiagramDnDProvider;
import com.intellij.diagram.settings.DiagramConfigElement;
import com.intellij.diagram.settings.DiagramConfigGroup;
import com.intellij.psi.PsiElement;
import com.intellij.uml.utils.DiagramBundle;
import org.jetbrains.annotations.NotNull;
import org.vepanimas.uml.javascript.actions.JavaScriptAddElementAction;
import org.vepanimas.uml.javascript.dependencies.JavaScriptUmlDependenciesSettingsOption;
import org.vepanimas.uml.javascript.providers.JavaScriptUmlClassImplementations;
import org.vepanimas.uml.javascript.providers.JavaScriptUmlClassParents;


public final class JavaScriptUmlExtras extends DiagramExtras<PsiElement> {

    private final JavaScriptUmlDnDProvider myUmlDnDProvider = new JavaScriptUmlDnDProvider();
    private final DiagramConfigGroup[] myAdditionalSettings;

    private static final DiagramElementsProvider<?>[] providers = {
            new JavaScriptUmlClassImplementations(),
            new JavaScriptUmlClassParents()
    };

    JavaScriptUmlExtras() {
        DiagramConfigGroup dependenciesGroup = new DiagramConfigGroup(getConfigGroupName());
        for (var option : JavaScriptUmlDependenciesSettingsOption.values()) {
            dependenciesGroup.addElement(new DiagramConfigElement(option.getDisplayName(), true));
        }
        myAdditionalSettings = new DiagramConfigGroup[]{dependenciesGroup};
    }

    @Override
    public DiagramDnDProvider<PsiElement> getDnDProvider() {
        return myUmlDnDProvider;
    }

    public DiagramAddElementAction getAddElementHandler() {
        return new JavaScriptAddElementAction();
    }

    @Override
    public DiagramConfigGroup @NotNull [] getAdditionalDiagramSettings() {
        return myAdditionalSettings;
    }

    @Override
    public boolean isExpandCollapseActionsImplemented() {
        return false;
    }

    public static @NotNull String getConfigGroupName() {
        return DiagramBundle.message("uml.dependencies.settings.group.title");
    }

    @Override
    public DiagramElementsProvider<PsiElement> @NotNull [] getElementsProviders() {
        //noinspection unchecked
        return (DiagramElementsProvider<PsiElement>[]) providers;
    }
}
