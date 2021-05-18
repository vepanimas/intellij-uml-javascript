package org.vepanimas.uml.javascript.providers;

import com.intellij.diagram.extras.providers.ImplementationsProvider;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import gnu.trove.THashSet;
import org.vepanimas.uml.javascript.JavaScriptUmlBundle;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class JavaScriptUmlClassImplementations extends ImplementationsProvider<PsiElement> {
    @Override
    public PsiElement[] getElements(PsiElement element, Project project) {
        JSClass clazz = (JSClass) element;
        Collection<PsiElement> inheritors = Collections.synchronizedSet(new THashSet<>());

        Processor<JSClass> p = jsClass -> {
            inheritors.add(jsClass);
            return true;
        };
        JSClassSearch.searchClassInheritors(clazz, true).forEach(p);
        if (clazz.isInterface()) {
            JSClassSearch.searchInterfaceImplementations(clazz, true).forEach(p);
        }
        return inheritors.toArray(PsiElement.EMPTY_ARRAY);
    }

    @Override
    public String getHeaderName(PsiElement element, Project project) {
        return JavaScriptUmlBundle.message("javascript.uml.show.implementations.header", ((JSClass) element).getName());
    }

    @Override
    public Comparator getComparator() {
        return PSI_COMPARATOR;
    }

    @Override
    public boolean isEnabledOn(PsiElement element) {
        return element instanceof JSClass;
    }
}
