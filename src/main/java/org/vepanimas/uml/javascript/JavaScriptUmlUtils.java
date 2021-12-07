package org.vepanimas.uml.javascript;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptEnum;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JavaScriptUmlUtils {
    public static @Nullable String getQualifiedName(@Nullable PsiElement element) {
        if (element == null) return null;

        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) return null;

        if (element instanceof JSClass) {
            String name = ((JSClass) element).getQualifiedName();
            if (name == null) return null;
            return String.join("#", virtualFile.getPath(), name);
        }
        if (element instanceof JSFile) {
            return virtualFile.getPath();
        }
        return null;
    }

    public static boolean isMethod(@Nullable Object element) {
        return element instanceof JSFunction && !isConstructor(element) && !isGetterOrSetter(element);
    }

    public static boolean isConstructor(@Nullable Object element) {
        return element instanceof JSFunction && ((JSFunction) element).isConstructor();
    }

    public static boolean isGetterOrSetter(@Nullable Object element) {
        return element instanceof JSFunction && (((JSFunction) element).isGetProperty() || ((JSFunction) element).isSetProperty());
    }

    public static boolean isReadWriteProperty(@Nullable Object element) {
        if (!(element instanceof JSFunctionItem)) {
            return false;
        }

        boolean isProperty = ((JSFunctionItem) element).isGetProperty() || ((JSFunctionItem) element).isSetProperty();
        if (!isProperty) {
            return false;
        }

        boolean hasSetProperty = false;
        boolean hasGetProperty = false;

        JSClass containingClass = ObjectUtils.tryCast(JSResolveUtil.findParent((JSFunctionItem) element), JSClass.class);
        if (containingClass == null) return false;
        String name = ((JSFunctionItem) element).getName();
        if (name == null) return false;

        for (JSElement member : containingClass.findMembersByName(name)) {
            if (member instanceof JSFunctionItem) {
                if (((JSFunctionItem) member).isGetProperty()) {
                    hasGetProperty = true;
                }
                if (((JSFunctionItem) member).isSetProperty()) {
                    hasSetProperty = true;
                }
            }
        }

        return hasGetProperty && hasSetProperty;
    }

    public static boolean isIgnoredProperty(@Nullable Object element) {
        if (element == null) return true;

        if (isReadWriteProperty(element)) {
            return ((JSFunctionItem) element).isSetProperty();
        }
        return false;
    }

    public static boolean isInterfaceOrObjectProperty(@Nullable Object element) {
        return element instanceof JSField && JSResolveUtil.findParent(((JSField) element)) instanceof TypeScriptInterface ||
                element instanceof TypeScriptPropertySignature;
    }

    public static boolean isEnumField(@Nullable Object element) {
        return element instanceof JSField && JSResolveUtil.findParent(((JSField) element)) instanceof TypeScriptEnum;
    }

    public static boolean isAcceptableAsSource(@Nullable Object object) {
        if (object instanceof PsiDirectory) return true;
        if (object instanceof JSFile) {
            return isSupportedLanguageDialect(((JSFile) object));
        }

        return isAcceptableAsNode(object);
    }

    public static boolean isAcceptableAsNode(@Nullable Object o) {
        if (o instanceof JSClass) {
            return isSupportedLanguageDialect(((JSClass) o));
        }

        return false;
    }

    private static boolean isSupportedLanguageDialect(@NotNull PsiElement element) {
        return DialectDetector.isJavaScript(element) || DialectDetector.isTypeScript(element);
    }

    public static boolean isSupportedFileType(@Nullable VirtualFile file) {
        if (file == null) return false;
        FileType fileType = file.getFileType();
        return DialectDetector.JAVASCRIPT_FILE_TYPES.contains(fileType) || TypeScriptUtil.TYPESCRIPT_FILE_TYPES.contains(fileType);
    }
}
