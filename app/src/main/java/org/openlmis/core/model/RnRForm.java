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

package org.openlmis.core.model;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ListUtil;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARegimeThreeLineList.CountType;

@Getter
@Setter
@DatabaseTable(tableName = "rnr_forms")
public class RnRForm extends BaseModel {

  public enum Status {
    DRAFT,
    SUBMITTED,
    AUTHORIZED,
    DRAFT_MISSED,
    SUBMITTED_MISSED,
  }

  @ForeignCollectionField()
  private ForeignCollection<RnrFormItem> rnrFormItemList;

  @Expose
  @SerializedName("products")
  private List<RnrFormItem> rnrFormItemListWrapper;

  @ForeignCollectionField()
  private ForeignCollection<RegimenItem> regimenItemList;

  @Expose
  @SerializedName("regimens")
  private List<RegimenItem> regimenItemListWrapper;

  @ForeignCollectionField()
  private ForeignCollection<RegimenItemThreeLines> regimenThreeLineList;
  @Expose
  @SerializedName("therapeuticLines")
  private List<RegimenItemThreeLines> regimenThreeLinesWrapper;

  @ForeignCollectionField()
  private ForeignCollection<BaseInfoItem> baseInfoItemList;

  @Expose
  @SerializedName("patientQuantifications")
  private List<BaseInfoItem> baseInfoItemListWrapper;

  @ForeignCollectionField()
  private ForeignCollection<RnRFormSignature> signatures;

  private List<RnRFormSignature> signaturesWrapper;

  @Expose
  @SerializedName("clientSubmittedNotes")
  @DatabaseField
  private String comments;

  @DatabaseField(defaultValue = "DRAFT")
  private Status status;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private Program program;

  @DatabaseField
  private boolean synced = false;

  @Expose
  @SerializedName("actualPeriodStartDate")
  @DatabaseField
  private Date periodBegin;

  @Expose
  @SerializedName("actualPeriodEndDate")
  @DatabaseField
  private Date periodEnd;

  @Expose
  @SerializedName("clientSubmittedTime")
  @DatabaseField
  private Date submittedTime;

  @Expose
  @DatabaseField
  private boolean emergency;

  public boolean isDraft() {
    return getStatus() == Status.DRAFT || getStatus() == Status.DRAFT_MISSED;
  }

  public boolean isMissed() {
    return getStatus() == Status.DRAFT_MISSED || getStatus() == Status.SUBMITTED_MISSED;
  }

  public boolean isSubmitted() {
    return getStatus() == Status.SUBMITTED || getStatus() == Status.SUBMITTED_MISSED;
  }

  public boolean canRemoveAddedProducts() {
    return isDraft() || isMissed() || isSubmitted();
  }

  public boolean isAuthorized() {
    return getStatus() == Status.AUTHORIZED;
  }

  public static RnRForm init(Program program, Date generateDate) {
    RnRForm rnrForm = new RnRForm();
    rnrForm.program = program;
    Period period = DateUtil.generateRnRFormPeriodBy(generateDate);
    rnrForm.periodBegin = period.getBegin().toDate();
    rnrForm.periodEnd = period.getEnd().toDate();
    return rnrForm;
  }

  public static RnRForm init(Program program, Period period, boolean isEmergency) {
    RnRForm rnRForm = new RnRForm();
    rnRForm.program = program;
    rnRForm.periodBegin = period.getBegin().toDate();
    rnRForm.periodEnd = period.getEnd().toDate();
    rnRForm.setEmergency(isEmergency);

    if (isMissedPeriod(period) && !isEmergency) {
      rnRForm.status = Status.DRAFT_MISSED;
    } else {
      rnRForm.status = Status.DRAFT;
    }

    return rnRForm;
  }

  public static long calculateTotalRegimenAmount(Collection<RegimenItem> list,
      MMIARegimeList.COUNTTYPE counttype) {
    long totalRegimenNumber = 0;
    if (MMIARegimeList.COUNTTYPE.PHARMACY == counttype) {
      for (RegimenItem item : list) {
        if (item.getPharmacy() != null) {
          totalRegimenNumber += item.getPharmacy();
        }
      }
    } else if (MMIARegimeList.COUNTTYPE.AMOUNT == counttype) {
      for (RegimenItem item : list) {
        if (item.getAmount() != null) {
          totalRegimenNumber += item.getAmount();
        }
      }
    }
    return totalRegimenNumber;
  }

  public static long caculateTotalRegimenTypeAmount(Collection<RegimenItemThreeLines> list,
      CountType counttype) {
    long totalNumber = 0;
    if (CountType.PATIENTSAMOUNT == counttype) {
      for (RegimenItemThreeLines item : list) {
        if (item.getPatientsAmount() != null) {
          totalNumber += item.getPatientsAmount();
        }
      }
    } else if (CountType.PHARMACYAMOUNT == counttype) {
      for (RegimenItemThreeLines item : list) {
        if (item.getPharmacyAmount() != null) {
          totalNumber += item.getPharmacyAmount();
        }
      }
    }
    return totalNumber;
  }

  public List<RnrFormItem> getRnrFormItemListWrapper() {
    rnrFormItemListWrapper = ListUtil.wrapOrEmpty(rnrFormItemList, rnrFormItemListWrapper);

    if (isAuthorized()) {
      sortRnrItemsListBasedOnProductCode(rnrFormItemListWrapper);
      return rnrFormItemListWrapper;
    } else {
      return sortProductList(rnrFormItemListWrapper);
    }
  }

  public List<BaseInfoItem> getBaseInfoItemListWrapper() {
    baseInfoItemListWrapper = ListUtil.wrapOrEmpty(baseInfoItemList, baseInfoItemListWrapper);
    return baseInfoItemListWrapper;
  }

  public List<RegimenItem> getRegimenItemListWrapper() {
    regimenItemListWrapper = ListUtil.wrapOrEmpty(regimenItemList, regimenItemListWrapper);
    return regimenItemListWrapper;
  }

  public List<RegimenItemThreeLines> getRegimenThreeLineListWrapper() {
    regimenThreeLinesWrapper = ListUtil.wrapOrEmpty(regimenThreeLineList, regimenThreeLinesWrapper);
    return regimenThreeLinesWrapper;
  }

  public List<RnRFormSignature> getSignaturesWrapper() {
    signaturesWrapper = ListUtil.wrapOrEmpty(signatures, signaturesWrapper);
    return signaturesWrapper;
  }

  public static void fillFormId(RnRForm rnRForm) {
    for (RnrFormItem item : rnRForm.getRnrFormItemListWrapper()) {
      item.setForm(rnRForm);
    }
    for (RegimenItem regimenItem : rnRForm.getRegimenItemListWrapper()) {
      regimenItem.setForm(rnRForm);
    }
    for (BaseInfoItem item : rnRForm.getBaseInfoItemListWrapper()) {
      item.setRnRForm(rnRForm);
    }
    for (RegimenItemThreeLines itemThreeLines : rnRForm.getRegimenThreeLineListWrapper()) {
      itemThreeLines.setForm(rnRForm);
    }
  }

  public List<RnrFormItem> getDeactivatedAndUnsupportedProductItems(
      final List<String> supportedProductCodes) {

    return from(getRnrFormItemListWrapper()).filter(
        rnrFormItem -> !(rnrFormItem.getProduct().isActive() && supportedProductCodes
            .contains(rnrFormItem.getProduct().getCode()))).toList();
  }

  public List<RnrFormItem> getRnrItems(final Product.IsKit isKit) {
    return from(getRnrFormItemListWrapper()).filter(rnrFormItem -> rnrFormItem.getProduct() != null)
        .filter(rnrFormItem -> isKit.isKit() == rnrFormItem.getProduct().isKit()).toList();
  }

  public void addSignature(String signature) {
    if (isDraft()) {
      getSignaturesWrapper().add(new RnRFormSignature(this, signature, Signature.TYPE.SUBMITTER));
      status = isMissed() ? Status.SUBMITTED_MISSED : Status.SUBMITTED;
    } else {
      getSignaturesWrapper().add(new RnRFormSignature(this, signature, Signature.TYPE.APPROVER));
      status = Status.AUTHORIZED;
      submittedTime = DateUtil.today();
    }
  }

  public boolean isOldMMIALayout() {
    return baseInfoItemList.size() == 7;
  }

  public enum Emergency {
    YES(true),
    NO(false);

    public boolean emergency() {
      return isEmergency;
    }

    private final boolean isEmergency;

    Emergency(boolean isEmergency) {
      this.isEmergency = isEmergency;
    }
  }


  private static boolean isMissedPeriod(Period period) {
    DateTime today = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
    DateTime periodEnd = period.getEnd();
    int monthOffset = DateUtil.calculateMonthOffset(today, periodEnd);
    return monthOffset > 0 || (monthOffset == 0
        && today.getDayOfMonth() >= Period.INVENTORY_END_DAY_NEXT);
  }

  private List<RnrFormItem> sortProductList(List<RnrFormItem> rnrFormItems) {
    List<RnrFormItem> existingList = newArrayList(
        from(rnrFormItems).filter(rnrFormItem -> !rnrFormItem.isManualAdd()).toList());

    List<RnrFormItem> newlyAddedList = newArrayList(
        from(rnrFormItems).filter(rnrFormItem -> rnrFormItem.isManualAdd()).toList());
    sortRnrItemsListBasedOnProductCode(existingList);
    sortRnrItemsListBasedOnProductCode(newlyAddedList);
    rnrFormItems = existingList;
    rnrFormItems.addAll(newlyAddedList);
    return rnrFormItems;
  }

  private void sortRnrItemsListBasedOnProductCode(List<RnrFormItem> rnrFormItems) {
    Collections.sort(rnrFormItems, (r1, r2) -> {
      if (r1.getProduct() != null && r2.getProduct() != null) {
        String code1 = r1.getProduct().getCode();
        String code2 = r2.getProduct().getCode();
        return code1.compareTo(code2);
      } else {
        return 0;
      }
    });
  }
}
