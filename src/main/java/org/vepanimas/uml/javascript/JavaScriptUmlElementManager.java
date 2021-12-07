package org.vepanimas.uml.javascript;

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.ElementBase;
import com.intellij.psi.util.PsiFormatUtilBase;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

import static com.intellij.psi.util.PsiFormatUtilBase.*;

public class JavaScriptUmlElementManager extends AbstractDiagramElementManager<PsiElement> {
    @Override
    public @Nullable PsiElement findInDataContext(@NotNull DataContext dataContext) {
        PsiElement element = dataContext.getData(CommonDataKeys.PSI_ELEMENT);
        if (JavaScriptUmlUtils.isAcceptableAsSource(element)) {
            return element;
        }

        PsiFile psiFile = dataContext.getData(CommonDataKeys.PSI_FILE);
        if (JavaScriptUmlUtils.isAcceptableAsSource(psiFile)) {
            return psiFile;
        }

        return null;
    }

    @Override
    public boolean canBeBuiltFrom(@Nullable Object element) {
        return JavaScriptUmlUtils.isAcceptableAsSource(element);
    }

    @Override
    public boolean isAcceptableAsNode(@Nullable Object o) {
        return JavaScriptUmlUtils.isAcceptableAsNode(o);
    }

    @Override
    public @Nullable @Nls String getElementTitle(PsiElement element) {
        if (element instanceof JSClass) {
            return ((JSClass) element).getQualifiedName();
        }
        if (element instanceof JSFile) {
            VirtualFile virtualFile = ((JSFile) element).getVirtualFile();
            if (virtualFile != null) {
                return virtualFile.getPresentableName();
            }
        }
        if (element instanceof PsiDirectory) {
            VirtualFile file = ((PsiDirectory) element).getVirtualFile();
            return JavaScriptUmlBundle
                    .message("javascript.uml.element.directory.presentable.name", file.getPresentableName());
        }
        return null;
    }

    @Override
    public @Nullable @Nls String getNodeTooltip(PsiElement element) {
        if (element instanceof JSClass) {
            return ((JSClass) element).getQualifiedName();
        }
        return null;
    }

    @Override
    public Object @NotNull [] getNodeItems(PsiElement parent) {
        if (parent instanceof JSClass) {
            Collection<JSElement> members = ContainerUtil.toCollection(((JSClass) parent).getMembers());
            return ContainerUtil.filter(members, this::shouldDisplayNode).toArray();
        }
        return PsiElement.EMPTY_ARRAY;
    }

    private boolean shouldDisplayNode(@NotNull JSElement element) {
        return !JavaScriptUmlUtils.isIgnoredProperty(element);
    }

    @Override
    public @Nullable SimpleColoredText getItemName(@Nullable PsiElement nodeElement, @Nullable Object nodeItem, @NotNull DiagramBuilder builder) {
        PsiNamedElement element = ObjectUtils.tryCast(nodeItem, PsiNamedElement.class);
        if (element == null) return null;

        String name = element.getName();
        if (StringUtil.isEmpty(name)) return null;

        StringBuilder text = new StringBuilder(name);
        if (element instanceof JSFunctionItem) {
            int options = SHOW_PARAMETERS | SHOW_FQ_CLASS_NAMES | PsiFormatUtilBase.TYPE_AFTER;
            int parametersOptions = PsiFormatUtilBase.SHOW_NAME | SHOW_TYPE | SHOW_RAW_TYPE | SHOW_FQ_CLASS_NAMES | PsiFormatUtilBase.TYPE_AFTER;
            String signature = JSFormatUtil.formatMethod(((JSFunctionItem) element), options, parametersOptions, MAX_PARAMS_TO_SHOW, null);
            text.append(signature);
        }

        return new SimpleColoredText(text.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    @Override
    public @Nullable SimpleColoredText getItemType(@Nullable Object element) {
        String typeText = getItemTypeText(element);
        return !StringUtil.isEmpty(typeText) ? new SimpleColoredText(typeText, SimpleTextAttributes.REGULAR_ATTRIBUTES) : null;
    }


    private @Nullable String getItemTypeText(@Nullable Object element) {
        if (!(element instanceof PsiElement)) return null;

        if (element instanceof JSFunction) {
            if (((JSFunction) element).isConstructor()) return null;

            JSType returnType = ((JSFunction) element).getReturnType();
            if (returnType != null) return returnType.getTypeText(JSType.TypeTextFormat.PRESENTABLE);
        }

        if (element instanceof JSField) {
            if (JavaScriptUmlUtils.isEnumField(element)) return null;

            JSType type = JSResolveUtil.getElementJSType((JSField) element);
            if (type != null) return type.getTypeText(JSType.TypeTextFormat.PRESENTABLE);
        }

        JSAnyType anyType = JSAnyType.get(((PsiElement) element), true);
        return anyType.getTypeText(JSType.TypeTextFormat.PRESENTABLE);
    }

    @Override
    public @Nullable Icon getItemIcon(@Nullable Object element, @NotNull DiagramState presentation) {
        int flags = Iconable.ICON_FLAG_READ_STATUS | Iconable.ICON_FLAG_VISIBILITY;

        if (JavaScriptUmlUtils.isReadWriteProperty(element)) {
            JSFunctionItem function = (JSFunctionItem) element;
            Icon initialIcon = function.getJSContext() == JSContext.STATIC ?
                    AllIcons.Nodes.PropertyReadWriteStatic :
                    AllIcons.Nodes.PropertyReadWrite;
            JSVisibilityUtil.PresentableAccessModifier modifier = JSVisibilityUtil.getPresentableAccessModifier(function);
            if (modifier == null) return initialIcon;
            return ElementBase.iconWithVisibilityIfNeeded(flags, initialIcon, modifier.getIcon());
        }

        if (element instanceof Iconable) {
            return ((Iconable) element).getIcon(flags);
        }

        return super.getItemIcon(element, presentation);
    }
}
