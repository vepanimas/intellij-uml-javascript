package org.vepanimas.uml.javascript;

import com.intellij.diagram.AbstractUmlVisibilityManager;
import com.intellij.diagram.VisibilityLevel;
import com.intellij.lang.javascript.psi.JSElementBase;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.psi.PsiModifier;
import com.intellij.uml.utils.DiagramBundle;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class JavaScriptUmlVisibilityManager extends AbstractUmlVisibilityManager {
    private static final VisibilityLevel @NotNull [] levels = {
            new VisibilityLevel(PsiModifier.PUBLIC),
            new VisibilityLevel(PsiModifier.PROTECTED),
            new VisibilityLevel(PsiModifier.PRIVATE, DiagramBundle.message("visibility.level.all"))
    };

    private static final @NotNull Comparator<VisibilityLevel> COMPARATOR = (o1, o2) -> {
        final int ind1 = ArrayUtil.indexOf(levels, o1);
        final int ind2 = ArrayUtil.indexOf(levels, o2);
        return ind1 == ind2 ? 0 : ind1 < 0 ? 1 : ind1 - ind2;
    };

    @Override
    public VisibilityLevel @NotNull [] getVisibilityLevels() {
        return levels;
    }

    @Override
    public @Nullable VisibilityLevel getVisibilityLevel(@Nullable Object element) {
        if (element instanceof JSElementBase) {
            JSAttributeList.AccessType accessType = ((JSElementBase) element).getAccessType();
            switch (accessType) {
                case PUBLIC:
                case PACKAGE_LOCAL:
                    return levels[0];
                case PROTECTED:
                    return levels[1];
                case PRIVATE:
                    return levels[2];
            }
        }
        return null;
    }

    @Override
    public @NotNull Comparator<VisibilityLevel> getComparator() {
        return COMPARATOR;
    }

    @Override
    public boolean isRelayoutNeeded() {
        return true;
    }
}
