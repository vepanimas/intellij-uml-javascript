package org.vepanimas.uml.javascript;

import com.intellij.diagram.actions.DiagramAddElementAction;
import com.intellij.diagram.extras.DiagramExtras;
import com.intellij.diagram.extras.providers.DiagramDnDProvider;
import com.intellij.psi.PsiElement;
import org.vepanimas.uml.javascript.actions.JavaScriptAddElementAction;


public final class JavaScriptUmlExtras extends DiagramExtras<PsiElement> {

    private final JavaScriptUmlDnDProvider myUmlDnDProvider = new JavaScriptUmlDnDProvider();

    @Override
    public DiagramDnDProvider<PsiElement> getDnDProvider() {
        return myUmlDnDProvider;
    }

    public DiagramAddElementAction getAddElementHandler() {
        return new JavaScriptAddElementAction();
    }

    @Override
    public boolean isExpandCollapseActionsImplemented() {
        return true;
    }
}
