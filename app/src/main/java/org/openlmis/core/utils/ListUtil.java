package org.openlmis.core.utils;

import com.j256.ormlite.dao.ForeignCollection;

import java.util.ArrayList;
import java.util.List;

public final class ListUtil {
    private ListUtil() {

    }

    public static <T> List<T> wrapOrEmpty(ForeignCollection<T> origin, List<T> target) {
        if (target == null) {
            return (origin == null ? new ArrayList<T>() : new ArrayList<>(origin));
        }
        return target;
    }
}
