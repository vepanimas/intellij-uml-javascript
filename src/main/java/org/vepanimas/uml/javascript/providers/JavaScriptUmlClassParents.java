package org.vepanimas.uml.javascript.providers;

import com.intellij.diagram.extras.providers.SupersProvider;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.vepanimas.uml.javascript.JavaScriptUmlBundle;

import java.util.Collection;
import java.util.Comparator;

public class JavaScriptUmlClassParents extends SupersProvider<PsiElement> {
    @Override
    public PsiElement[] getElements(PsiElement element, Project project) {
        return JSInheritanceUtil.findAllParentsForClass((JSClass) element, true).toArray(JSClass.EMPTY_ARRAY);
    }

    @Override
    public boolean isEnabledOn(PsiElement element) {
        return element instanceof JSClass;
    }

    @Override
    public String getHeaderName(PsiElement element, Project project) {
        return JavaScriptUmlBundle.message("javascript.uml.show.supers.header", ((JSClass) element).getName());
    }

    @Override
    public Comparator getComparator() {
        return PSI_COMPARATOR;
    }
}
