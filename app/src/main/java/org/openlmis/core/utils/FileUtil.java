/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.utils;

import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openlmis.core.exceptions.LMISException;

public final class FileUtil {

  private FileUtil() {
  }

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

  public static void copy(File srcFile, File dstFile) throws IOException {
    try (FileChannel src = new FileInputStream(srcFile)
        .getChannel(); FileChannel dst = new FileOutputStream(dstFile).getChannel()) {
      dst.transferFrom(src, 0, src.size());
    }
  }

  public static void copyInputStreamToFile(InputStream in, File file) {
    try (PrintWriter writer = new PrintWriter(file,
        "UTF_8"); OutputStream out = new FileOutputStream(
        file)) {
      writer.print("");
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
    } catch (Exception e) {
      new LMISException(e, "FileUtil.copyInputStreamToFile").reportToFabric();
    }
  }

  public static void createNewExcel(String filePath, String fileName, HSSFWorkbook hssfWorkbook) {
    try {
      File file = createNewFileWithoutDuplication(filePath, fileName);

      FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsoluteFile());
      hssfWorkbook.write(fileOutputStream);

      fileOutputStream.flush();
      fileOutputStream.close();
    } catch (IOException e) {
      new LMISException(e, "FileUtil.createExcel").reportToFabric();
    }
  }

  @NonNull
  public static File createNewFileWithoutDuplication(String filePath, String fileName)
      throws IOException {
    File dir = new File(filePath);
    dir.mkdirs();

    File file = new File(filePath + File.separator + fileName);

    int newFileNameIndex = 1;
    String newFileName;
    while (file.exists()) {
      int suffixIndex = fileName.lastIndexOf(".");
      newFileName = fileName.substring(0, suffixIndex)
          + "(" + newFileNameIndex++ + ")"
          + fileName.substring(suffixIndex);
      file = new File(filePath + File.separator + newFileName);
    }
    file.createNewFile();
    return file;
  }

}
