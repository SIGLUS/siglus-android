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

import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
        "UTF_8"); OutputStream out = new FileOutputStream(file)) {
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

  @Nullable
  public static File createNewExcel(String filePath, String fileName, HSSFWorkbook hssfWorkbook) {
    FileOutputStream fileOutputStream = null;
    try {
      File file = createNewFileWithoutDuplication(filePath, fileName);

      fileOutputStream = new FileOutputStream(file.getAbsoluteFile());
      hssfWorkbook.write(fileOutputStream);

      fileOutputStream.flush();

      return file;
    } catch (IOException e) {
      reportException(e);
    } finally {
      if (fileOutputStream != null) {
        try {
          fileOutputStream.close();
        } catch (IOException e) {
          reportException(e);
        }
      }
    }
    return null;
  }

  public static void transExcelToPdf(String excelFilePath, String fileName) {
    String pdfFileName = fileName + ".pdf";
    try {
      File pdfFilePath = createNewFileWithoutDuplication(
          Environment.getExternalStorageDirectory().getAbsolutePath(), pdfFileName
      );

      FileOutputStream pdfFile = new FileOutputStream(pdfFilePath);
      Document pdfDocument = new Document(PageSize.A4);
      pdfDocument.setMargins(20, 20, 50, 50);
      PdfWriter.getInstance(pdfDocument, pdfFile);
      pdfDocument.open();

      FileInputStream excelFile = new FileInputStream(excelFilePath);
      Workbook workbook = new HSSFWorkbook(excelFile);
      Sheet sheet = workbook.getSheetAt(0);

      for (Row row : sheet) {
        int columnNum = row.getPhysicalNumberOfCells();
        if (columnNum == 0) {
          continue;
        }

        PdfPTable pdfTable = new PdfPTable(columnNum);
        pdfTable.setWidthPercentage(100);
        for (Cell cell : row) {
          String cellValue = getCellValueString(cell);
          PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue));
          pdfCell.setBorder(PdfPCell.BOX);
          pdfTable.addCell(pdfCell);
        }

        pdfDocument.add(pdfTable);
      }

      pdfDocument.close();
    } catch (IOException e) {
      reportException(e);
    }
  }

  private static String getCellValueString(Cell cell) {
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        return DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue().toString() :
            String.valueOf(cell.getNumericCellValue());
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case FORMULA:
        return cell.getCellFormula();
      case BLANK:
      default:
        return "";
    }
  }

  private static void reportException(IOException e) {
    new LMISException(e, "FileUtil.createExcel").reportToFabric();
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
      int suffixIndex = fileName.lastIndexOf('.');
      newFileName = fileName.substring(0, suffixIndex)
          + "(" + newFileNameIndex++ + ")"
          + fileName.substring(suffixIndex);
      file = new File(filePath + File.separator + newFileName);
    }
    file.createNewFile();
    return file;
  }

}
