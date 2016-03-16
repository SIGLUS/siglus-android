package org.openlmis.core.utils;

import java.io.File;

public final class FileUtil {

    private FileUtil() {}

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }

        if (dir.isDirectory()) {
            for (String directory : dir.list()) {
                boolean success = deleteDir(new File(dir, directory));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
