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

package org.openlmis.core.presenter;

import static org.openlmis.core.manager.MovementReasonManager.EXPIRED_RETURN_TO_SUPPLIER_AND_DISCARD;
import static org.openlmis.core.utils.DateUtil.DATE_TIME_WITH_AM_MARKER_FORMAT;
import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;
import static org.openlmis.core.utils.DateUtil.DOCUMENT_NO_DATE_TIME_FORMAT;
import static org.openlmis.core.utils.DateUtil.SIMPLE_DATE_FORMAT;
import static org.openlmis.core.utils.FileUtil.createNewExcel;
import static org.openlmis.core.utils.FileUtil.transExcelToPdf;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.os.Environment;
import androidx.annotation.NonNull;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.LotExcelModel;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ExpiredStockCardListPresenter extends StockCardPresenter {

  private String excelFilePath;

  @Override
  public void attachView(BaseView v) {
    view = (ExpiredStockCardListView) v;
  }

  public void loadExpiredStockCards() {
    view.loading();

    Subscription subscription = loadExpiredStockCardsObservable().subscribe(afterLoadHandler);
    subscriptions.add(subscription);
  }

  private Observable<List<StockCard>> loadExpiredStockCardsObservable() {
    return Observable.create((OnSubscribe<List<StockCard>>) subscriber -> {
      subscriber.onNext(loadExpiredStockCardsFromDB());
      subscriber.onCompleted();
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  private List<StockCard> loadExpiredStockCardsFromDB() {
    return from(stockRepository.list()).filter(stockCard -> {
      if (stockCard != null && isActiveProduct(stockCard)
          && !isArchivedProduct(stockCard)) {
        List<LotOnHand> expiredLots = filterExpiredAndNonEmptyLot(stockCard);
        if (expiredLots.size() > 0) {
          lotsOnHands.put(
              String.valueOf(stockCard.getId()),
              String.valueOf(stockCard.calculateSOHFromLots(expiredLots))
          );
          stockCard.setLotOnHandListWrapper(expiredLots);
          return true;
        }
      }
      return false;
    }).toList();
  }

  private List<LotOnHand> filterExpiredAndNonEmptyLot(StockCard stockCard) {
    List<LotOnHand> lotOnHandListWrapper = stockCard.getLotOnHandListWrapper();
    return from(lotOnHandListWrapper)
        .filter(lotOnHand -> lotOnHand.getLot().isExpired() && lotOnHand.getQuantityOnHand() > 0)
        .toList();
  }

  public boolean isCheckedLotsExisting() {
    for (InventoryViewModel inventoryViewModel : inventoryViewModels) {
      List<LotOnHand> lots = inventoryViewModel.getStockCard().getLotOnHandListWrapper();

      for (LotOnHand lotOnHand : lots) {
        if (lotOnHand.isChecked()) {
          return true;
        }
      }
    }

    return false;
  }

  public void handleCheckedExpiredProducts(String sign) {
    view.loading();

    Subscription subscribe = handleExpiredStocksObservable(sign).subscribe(
        new Observer<List<StockCard>>() {
          @Override
          public void onCompleted() {
            afterLoadHandler.onCompleted();
          }

          @Override
          public void onError(Throwable e) {
            afterLoadHandler.onError(e);
          }

          @Override
          public void onNext(List<StockCard> stockCards) {
            afterLoadHandler.onNext(stockCards);
            if (excelFilePath != null) {
              ((ExpiredStockCardListView) view).showHandleCheckedExpiredResult(excelFilePath);
            }
          }
        }
    );

    subscriptions.add(subscribe);
  }

  private Observable<List<StockCard>> handleExpiredStocksObservable(String sign) {
    return Observable.create((OnSubscribe<List<StockCard>>) subscriber -> {
      List<LotOnHand> checkedLots = getCheckedLots();
      Date currentDate = DateUtil.getCurrentDate();

      try {
        // 1. handle the removed lots - negative adjustment
        stockRepository.addStockMovementsAndUpdateStockCards(
            convertLotOnHandsToStockMovementItems(checkedLots, sign, currentDate)
        );
        // 2. generate excel
        excelFilePath = generateExcelReport(checkedLots, sign, currentDate);
        // 3. generate pdf from excel
        int lastSlashIndex = excelFilePath.lastIndexOf("/");
        int lastDotIndex = excelFilePath.lastIndexOf(".");
        String fileName = excelFilePath.substring(lastSlashIndex + 1, lastDotIndex);
        transExcelToPdf(excelFilePath, fileName);
      } catch (LMISException e) {
        subscriber.onError(e);
      }

      // 3. refresh data
      subscriber.onNext(loadExpiredStockCardsFromDB());
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  String generateExcelReport(
      List<LotOnHand> checkedLotOnHands,
      String signature,
      Date currentDate
  ) {
    HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
    HSSFSheet hssfSheet = hssfWorkbook.createSheet("Sheet01");
    // width
    int width = 24 * 256;
    for (int columnIndex = 0; columnIndex < 10; columnIndex++) {
      hssfSheet.setColumnWidth(columnIndex, width);
    }
    // border style
    HSSFCellStyle borderStyle = createBoarderStyle(hssfWorkbook);

    // Summary part
    int rowStartIndex = 0;
    UserInfoMgr userInfoMgr = UserInfoMgr.getInstance();
    String facilityName = userInfoMgr.getFacilityName();
    String provinceName = userInfoMgr.getProvinceName();
    String districtName = userInfoMgr.getDistrictName();
    rowStartIndex = generateExcelSummary(
        rowStartIndex, hssfWorkbook, hssfSheet, borderStyle, currentDate, facilityName,
        provinceName, districtName
    );
    // Filled in by the supplier
    hssfSheet.createRow(rowStartIndex++);

    List<LotExcelModel> lotExcelModels = from(checkedLotOnHands).transform(
        lotOnHand -> lotOnHand.convertToExcelModel(
            "", "", String.valueOf(lotOnHand.getQuantityOnHand())
        )
    ).toList();

    rowStartIndex = generateExcelFilledBySupplier(
        rowStartIndex, hssfSheet, borderStyle, lotExcelModels
    );
    // Filled in by the client
    hssfSheet.createRow(rowStartIndex++);

    rowStartIndex = generateExcelFilledByClient(rowStartIndex, hssfSheet, borderStyle);
    // Total value
    hssfSheet.createRow(rowStartIndex++);

    rowStartIndex = generateTotalValue(rowStartIndex, hssfSheet, borderStyle, lotExcelModels);
    // Signature
    hssfSheet.createRow(rowStartIndex++);

    rowStartIndex = generateExcelSignature(rowStartIndex, signature, hssfSheet, borderStyle);
    // Date
    hssfSheet.createRow(rowStartIndex++);

    generateExcelDate(rowStartIndex, currentDate, hssfSheet);
    // generate excel file
    File excelFile = createNewExcel(
        Environment.getExternalStorageDirectory().getAbsolutePath(),
        generateExcelFileName(currentDate, facilityName),
        hssfWorkbook
    );
    return excelFile == null ? null : excelFile.getAbsolutePath();
  }

  @NonNull
  private HSSFCellStyle createBoarderStyle(HSSFWorkbook hssfWorkbook) {
    HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
    BorderStyle thinBoarder = BorderStyle.THIN;
    borderStyle.setBorderBottom(thinBoarder);
    borderStyle.setBorderTop(thinBoarder);
    borderStyle.setBorderRight(thinBoarder);
    borderStyle.setBorderLeft(thinBoarder);
    return borderStyle;
  }

  @NonNull
  private String generateExcelFileName(Date currentDate, String facilityName) {
    return facilityName.replace(" ", "_")
        + "_"
        + DateUtil.formatDate(currentDate, DB_DATE_FORMAT)
        + ".xls";
  }

  private void generateExcelDate(int rowStartIndex, Date currentDate, HSSFSheet hssfSheet) {
    HSSFRow dateRow = hssfSheet.createRow(rowStartIndex);
    dateRow.createCell(0).setCellValue(
        DateUtil.formatDate(currentDate, DATE_TIME_WITH_AM_MARKER_FORMAT)
    );
  }

  private int generateExcelSignature(
      int rowStartIndex,
      String signature,
      HSSFSheet hssfSheet,
      HSSFCellStyle borderStyle
  ) {
    // by row
    HSSFRow byRow = hssfSheet.createRow(rowStartIndex++);
    HSSFCell preparedByCell = byRow.createCell(0);
    preparedByCell.setCellValue("Prepared by: ");
    preparedByCell.setCellStyle(borderStyle);
    HSSFCell preparedByValueCell = byRow.createCell(1);
    preparedByValueCell.setCellValue(signature);
    preparedByValueCell.setCellStyle(borderStyle);
    HSSFCell conferredByCell = byRow.createCell(2);
    conferredByCell.setCellValue("Conferred by:");
    conferredByCell.setCellStyle(borderStyle);
    byRow.createCell(3).setCellStyle(borderStyle);
    HSSFCell receivedByCell = byRow.createCell(4);
    receivedByCell.setCellValue("Received by:");
    receivedByCell.setCellStyle(borderStyle);
    byRow.createCell(5).setCellStyle(borderStyle);
    // signature row
    HSSFRow signatureRow = hssfSheet.createRow(rowStartIndex++);
    HSSFCell preparedSignatureCell = signatureRow.createCell(0);
    String signatureTitle = "Signature:";
    preparedSignatureCell.setCellValue(signatureTitle);
    preparedSignatureCell.setCellStyle(borderStyle);
    signatureRow.createCell(1).setCellStyle(borderStyle);
    HSSFCell conferredSignatureCell = signatureRow.createCell(2);
    conferredSignatureCell.setCellValue(signatureTitle);
    conferredSignatureCell.setCellStyle(borderStyle);
    signatureRow.createCell(3).setCellStyle(borderStyle);
    HSSFCell receivedSignatureCell = signatureRow.createCell(4);
    receivedSignatureCell.setCellValue(signatureTitle);
    receivedSignatureCell.setCellStyle(borderStyle);
    signatureRow.createCell(5).setCellStyle(borderStyle);

    return rowStartIndex;
  }

  private int generateTotalValue(
      int rowStartIndex,
      HSSFSheet hssfSheet,
      HSSFCellStyle borderStyle,
      List<LotExcelModel> lotExcelModels
  ) {
    // title
    HSSFRow totalValueTitleRow = hssfSheet.createRow(rowStartIndex++);
    HSSFCell totalTitleCell = totalValueTitleRow.createCell(0);
    totalTitleCell.setCellValue("Total value of the issue voucher");
    totalTitleCell.setCellStyle(borderStyle);
    // value
    BigDecimal totalValue = BigDecimal.ZERO;
    for (LotExcelModel lotExcelModel : lotExcelModels) {
      totalValue = totalValue.add(new BigDecimal(lotExcelModel.getTotalValue()));
    }
    HSSFCell totalValueCell = totalValueTitleRow.createCell(1);
    totalValueCell.setCellValue(String.valueOf(totalValue));
    totalValueCell.setCellStyle(borderStyle);

    return rowStartIndex;
  }

  private int generateExcelFilledByClient(
      int rowStartIndex,
      HSSFSheet hssfSheet,
      HSSFCellStyle borderStyle
  ) {
    hssfSheet.createRow(rowStartIndex++).createCell(0).setCellValue("Filled in by the supplier");

    HSSFRow clientTitleRow = hssfSheet.createRow(rowStartIndex++);
    HSSFCell receivedQuantityCell = clientTitleRow.createCell(0);
    receivedQuantityCell.setCellValue("Received Quantity");
    receivedQuantityCell.setCellStyle(borderStyle);
    HSSFCell differenceCell = clientTitleRow.createCell(1);
    differenceCell.setCellValue("Difference");
    differenceCell.setCellStyle(borderStyle);

    HSSFRow clientTitleRow2 = hssfSheet.createRow(rowStartIndex++);
    HSSFCell qrCell = clientTitleRow2.createCell(0);
    qrCell.setCellValue("QR");
    qrCell.setCellStyle(borderStyle);
    HSSFCell qrQfCell = clientTitleRow2.createCell(1);
    qrQfCell.setCellValue("QR-QF");
    qrQfCell.setCellStyle(borderStyle);

    HSSFRow clientValueRow = hssfSheet.createRow(rowStartIndex++);
    clientValueRow.createCell(0).setCellStyle(borderStyle);
    clientValueRow.createCell(1).setCellStyle(borderStyle);

    return rowStartIndex;
  }

  private int generateExcelFilledBySupplier(
      int rowStartIndex,
      HSSFSheet hssfSheet,
      CellStyle borderStyle,
      List<LotExcelModel> lotExcelModels
  ) {
    rowStartIndex = generateExcelTitleOfLots(rowStartIndex, hssfSheet, borderStyle);

    rowStartIndex = generateExcelValueOfLots(
        rowStartIndex, hssfSheet, borderStyle, lotExcelModels
    );

    return rowStartIndex;
  }

  private int generateExcelValueOfLots(
      int rowStartIndex,
      HSSFSheet hssfSheet,
      CellStyle borderStyle,
      List<LotExcelModel> lotExcelModels
  ) {
    int lotsRowIndex = 1;
    short height = 2 * 256;
    for (LotExcelModel lotExcelModel : lotExcelModels) {
      HSSFRow lotRow = hssfSheet.createRow(rowStartIndex++);
      lotRow.setHeight(height);

      lotsRowIndex = generateExcelLot(borderStyle, lotsRowIndex, lotExcelModel, lotRow);
    }

    return rowStartIndex;
  }

  private int generateExcelTitleOfLots(int rowStartIndex, HSSFSheet hssfSheet,
      CellStyle borderStyle) {
    HSSFRow templateTitleOfRemovedLotsRow = hssfSheet.createRow(rowStartIndex++);
    HSSFCell templateTitleOfRemovedLotsColumn = templateTitleOfRemovedLotsRow.createCell(0);
    templateTitleOfRemovedLotsColumn.setCellValue("Filled in by the supplier");

    ArrayList<List<String>> templateForRemovedLots = new ArrayList<>();
    templateForRemovedLots.add(newArrayList());
    templateForRemovedLots.add(newArrayList("FNM"));
    templateForRemovedLots.add(newArrayList("Product"));
    templateForRemovedLots.add(newArrayList("Lot Number"));
    templateForRemovedLots.add(newArrayList("Expiring Date"));
    templateForRemovedLots.add(newArrayList("Ordered Quantity", ""));
    templateForRemovedLots.add(newArrayList("Partial Fulfilled", ""));
    templateForRemovedLots.add(newArrayList("Supplied Quantity", "QF"));
    templateForRemovedLots.add(newArrayList("Price", "PU"));
    templateForRemovedLots.add(newArrayList("Value", "QF X PU"));

    HSSFRow titleOfRemovedLotsRow = hssfSheet.createRow(rowStartIndex++);
    HSSFRow titleOfRemovedLotsRow2 = hssfSheet.createRow(rowStartIndex++);
    int lotsColumnStartIndex = 0;
    for (List<String> titles : templateForRemovedLots) {
      HSSFCell titleOfRemovedLotsCell = titleOfRemovedLotsRow.createCell(lotsColumnStartIndex);
      HSSFCell titleOfRemovedLotsCell2 = titleOfRemovedLotsRow2.createCell(lotsColumnStartIndex);
      titleOfRemovedLotsCell.setCellStyle(borderStyle);
      titleOfRemovedLotsCell2.setCellStyle(borderStyle);

      int firstRow = rowStartIndex - 2;
      int lastRow = rowStartIndex - 1;
      if (titles.size() == 0) {
        hssfSheet.addMergedRegion(new CellRangeAddress(
            firstRow, lastRow, lotsColumnStartIndex, lotsColumnStartIndex)
        );
      } else if (titles.size() == 1) {
        titleOfRemovedLotsCell.setCellValue(titles.get(0));
        hssfSheet.addMergedRegion(new CellRangeAddress(
            firstRow, lastRow, lotsColumnStartIndex, lotsColumnStartIndex)
        );
      } else {
        titleOfRemovedLotsCell.setCellValue(titles.get(0));
        titleOfRemovedLotsCell2.setCellValue(titles.get(1));
      }
      lotsColumnStartIndex++;
    }

    return rowStartIndex;
  }

  private int generateExcelLot(
      CellStyle borderStyle,
      int lotsRowIndex,
      LotExcelModel lotExcelModel,
      HSSFRow lotRow
  ) {
    int columnIndex = 0;
    HSSFCell indexCell = lotRow.createCell(columnIndex++);
    indexCell.setCellValue(lotsRowIndex++);
    indexCell.setCellStyle(borderStyle);
    HSSFCell productCodeCell = lotRow.createCell(columnIndex++);
    productCodeCell.setCellValue(lotExcelModel.getProductCode());
    productCodeCell.setCellStyle(borderStyle);
    HSSFCell productNameCell = lotRow.createCell(columnIndex++);
    productNameCell.setCellValue(lotExcelModel.getProductName());
    productNameCell.setCellStyle(borderStyle);
    HSSFCell lotNumberCell = lotRow.createCell(columnIndex++);
    lotNumberCell.setCellValue(lotExcelModel.getLotNumber());
    lotNumberCell.setCellStyle(borderStyle);
    HSSFCell expiringDateCell = lotRow.createCell(columnIndex++);
    expiringDateCell.setCellValue(lotExcelModel.getExpirationDate());
    expiringDateCell.setCellStyle(borderStyle);

    lotRow.createCell(columnIndex++).setCellStyle(borderStyle);
    lotRow.createCell(columnIndex++).setCellStyle(borderStyle);

    HSSFCell suppliedQuantityCell = lotRow.createCell(columnIndex++);
    suppliedQuantityCell.setCellValue(lotExcelModel.getSuppliedQuantity());
    suppliedQuantityCell.setCellStyle(borderStyle);
    HSSFCell priceCell = lotRow.createCell(columnIndex++);
    priceCell.setCellValue(lotExcelModel.getPrice());
    priceCell.setCellStyle(borderStyle);
    HSSFCell totalValueCell = lotRow.createCell(columnIndex);
    totalValueCell.setCellValue(lotExcelModel.getTotalValue());
    totalValueCell.setCellStyle(borderStyle);

    return lotsRowIndex;
  }

  private int generateExcelSummary(
      int rowStartIndex,
      HSSFWorkbook hssfWorkbook,
      HSSFSheet hssfSheet,
      HSSFCellStyle borderStyle,
      Date currentDate,
      String facilityName,
      String provinceName,
      String districtName
  ) {
    LinkedHashMap<String, String> templateForSupplier = new LinkedHashMap<>();
    templateForSupplier.put("Document number:", generateDocumentNumber(currentDate));
    templateForSupplier.put("Supplier:", facilityName);
    templateForSupplier.put("Client:", "");
    templateForSupplier.put("District: ", districtName);
    templateForSupplier.put("Province:", provinceName);
    templateForSupplier.put(
        "Issue voucher date:", DateUtil.formatDate(currentDate, SIMPLE_DATE_FORMAT)
    );
    String requisitionNumberKey = "Requisition Nr:";
    templateForSupplier.put(requisitionNumberKey, "");
    templateForSupplier.put("Total of volume:", "");
    templateForSupplier.put("Requisition date:", "");
    templateForSupplier.put("Supply", "");
    templateForSupplier.put("Returned", "");
    templateForSupplier.put("Expired", "");
    templateForSupplier.put("Overstocked", "");
    templateForSupplier.put("Reception date:", "");

    HSSFCellStyle backgroundAndBoarderStyle = createBoarderStyle(hssfWorkbook);
    backgroundAndBoarderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    backgroundAndBoarderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    boolean startSetBackgroundStyleForTitle = false;
    for (String title : templateForSupplier.keySet()) {
      HSSFRow row = hssfSheet.createRow(rowStartIndex);

      HSSFCell titleCell = row.createCell(0);
      // set background color for the titles which are below of `Requisition Nr:`
      if (!startSetBackgroundStyleForTitle && requisitionNumberKey.equals(title)) {
        startSetBackgroundStyleForTitle = true;
      }
      titleCell.setCellValue(title);
      if (startSetBackgroundStyleForTitle) {
        titleCell.setCellStyle(backgroundAndBoarderStyle);
      } else {
        titleCell.setCellStyle(borderStyle);
      }

      HSSFCell valueCell = row.createCell(1);
      valueCell.setCellValue(templateForSupplier.get(title));
      valueCell.setCellStyle(borderStyle);

      rowStartIndex++;
    }

    return rowStartIndex;
  }

  List<StockMovementItem> convertLotOnHandsToStockMovementItems(
      List<LotOnHand> checkedLots,
      String signature,
      Date currentDate
  ) {
    ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

    HashMap<StockCard, List<LotOnHand>> stockCardLotOnHandHashMap = connectStockCardAndLotOnHands(
        checkedLots);

    for (StockCard stockCard : stockCardLotOnHandHashMap.keySet()) {
      List<LotOnHand> lotOnHands = stockCardLotOnHandHashMap.get(stockCard);
      if (lotOnHands != null) {

        StockMovementItem stockMovementItem = convertLotOnHandsToStockMovementItem(
            stockCard,
            lotOnHands,
            signature,
            currentDate
        );

        stockMovementItems.add(stockMovementItem);
      }
    }

    return stockMovementItems;
  }

  @NonNull
  private StockMovementItem convertLotOnHandsToStockMovementItem(
      StockCard stockCard,
      @NonNull List<LotOnHand> lotOnHands,
      String signature,
      Date currentDate) {
    // quality
    long movementQuality = 0L;
    for (LotOnHand lotOnHand : lotOnHands) {
      movementQuality += lotOnHand.getQuantityOnHand();
    }
    final long latestStockOnHand = stockCard.getStockOnHand() - movementQuality;
    // documentNumber
    String documentNumber = generateDocumentNumber(currentDate);
    // reason
    final String reason = EXPIRED_RETURN_TO_SUPPLIER_AND_DISCARD;

    stockCard.setStockOnHand(latestStockOnHand);

    StockMovementItem stockMovementItem = new StockMovementItem(
        documentNumber, movementQuality, reason, MovementType.NEGATIVE_ADJUST, stockCard,
        latestStockOnHand, signature, currentDate, currentDate, currentDate
    );

    stockMovementItem.setLotMovementItemListWrapper(from(lotOnHands).transform(lotOnHand -> {
      return new LotMovementItem(
          lotOnHand.getLot(), lotOnHand.getQuantityOnHand(), stockMovementItem, reason,
          documentNumber);
    }).toList());

    return stockMovementItem;
  }

  private HashMap<StockCard, List<LotOnHand>> connectStockCardAndLotOnHands(
      List<LotOnHand> allLotOnHands) {
    HashMap<StockCard, List<LotOnHand>> stockCardLotOnHandHashMap = new HashMap<>();

    for (LotOnHand lotOnHand : allLotOnHands) {
      StockCard stockCard = lotOnHand.getStockCard();
      List<LotOnHand> lotOnHands = stockCardLotOnHandHashMap.get(stockCard);
      if (lotOnHands == null) {
        ArrayList<LotOnHand> newLotOnHands = new ArrayList<>();
        newLotOnHands.add(lotOnHand);
        stockCardLotOnHandHashMap.put(stockCard, newLotOnHands);
      } else {
        lotOnHands.add(lotOnHand);
      }
    }

    return stockCardLotOnHandHashMap;
  }

  @NonNull
  private String generateDocumentNumber(Date currentDate) {
    return UserInfoMgr.getInstance().getFacilityCode() + "_"
        + DateUtil.formatDate(currentDate, DOCUMENT_NO_DATE_TIME_FORMAT);
  }

  private List<LotOnHand> getCheckedLots() {
    List<LotOnHand> checkedLots = new ArrayList<>();

    for (InventoryViewModel inventoryViewModel : inventoryViewModels) {
      checkedLots.addAll(
          from(inventoryViewModel.getStockCard().getLotOnHandListWrapper())
              .filter(lotOnHand -> lotOnHand.isChecked())
              .toList()
      );
    }

    return checkedLots;
  }

  public interface ExpiredStockCardListView extends StockCardListView {

    void showHandleCheckedExpiredResult(String excelPath);
  }
}