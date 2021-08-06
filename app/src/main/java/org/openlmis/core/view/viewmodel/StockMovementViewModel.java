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

package org.openlmis.core.view.viewmodel;


import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.widget.NewMovementLotListView.LotStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockMovementViewModel extends BaseStockMovementViewModel {

    MovementReasonManager.MovementReason reason;

    String movementDate;
    String documentNo;
    String signature;
    boolean isKit;
    String requested;

    //old
    String stockExistence;
    boolean isDraft = true;

    //new
    private HashMap<MovementReasonManager.MovementType, String> typeQuantityMap = new HashMap<>();

    public StockMovementViewModel(StockMovementItem item) {
        product = item.getStockCard().getProduct();
        movementDate = DateUtil.formatDate(item.getMovementDate());
        documentNo = item.getDocumentNumber();
        stockExistence = String.valueOf(item.getStockOnHand());
        signature = item.getSignature();
        if (null == item.getRequested()) {
            requested = "";
        } else {
            requested = String.valueOf(item.getRequested());
        }
        isDraft = false;

        try {
            reason = MovementReasonManager.getInstance().queryByCode(item.getReason());
        } catch (MovementReasonNotFoundException e) {
            throw new RuntimeException("MovementReason Cannot be find " + e.getMessage());
        }

        typeQuantityMap.put(item.getMovementType(), String.valueOf(item.getMovementQuantity()));
    }

    public String getReceived() {
        return typeQuantityMap.get(MovementReasonManager.MovementType.RECEIVE);
    }

    public void setReceived(String received) {
        typeQuantityMap.put(MovementReasonManager.MovementType.RECEIVE, received);
    }

    public String getIssued() {
        return typeQuantityMap.get(MovementReasonManager.MovementType.ISSUE);
    }

    public void setIssued(String issued) {
        typeQuantityMap.put(MovementReasonManager.MovementType.ISSUE, issued);
    }

    public String getNegativeAdjustment() {
        return typeQuantityMap.get(MovementReasonManager.MovementType.NEGATIVE_ADJUST);
    }

    public void setNegativeAdjustment(String negativeAdjustment) {
        typeQuantityMap.put(MovementReasonManager.MovementType.NEGATIVE_ADJUST, negativeAdjustment);
    }

    public String getPositiveAdjustment() {
        return typeQuantityMap.get(MovementReasonManager.MovementType.POSITIVE_ADJUST);
    }

    public void setPositiveAdjustment(String positiveAdjustment) {
        typeQuantityMap.put(MovementReasonManager.MovementType.POSITIVE_ADJUST, positiveAdjustment);
    }

    public StockMovementItem convertViewToModel(StockCard stockCard) {
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setStockOnHand(Long.parseLong(getStockExistence()));

        stockMovementItem.setReason(reason.getCode());
        stockMovementItem.setDocumentNumber(getDocumentNo());
        stockMovementItem.setMovementType(reason.getMovementType());

        if (isKit) {
            Long movementQuantity = Long.parseLong(typeQuantityMap.get(reason.getMovementType()));
            stockMovementItem.setMovementQuantity(movementQuantity);
        }

        stockMovementItem.setRequested((null == requested || requested.isEmpty()) ? null : Long.valueOf(requested));

        stockMovementItem.setSignature(signature);

        stockMovementItem.setMovementDate(DateUtil.parseString(getMovementDate(), DateUtil.DEFAULT_DATE_FORMAT));

        stockMovementItem.setStockCard(stockCard);

        List<LotMovementViewModel> totalLotMovementViewModelList = new ArrayList<>();
        totalLotMovementViewModelList.addAll(existingLotMovementViewModelList);
        totalLotMovementViewModelList.addAll(newLotMovementViewModelList);
        if (!isKit) {
            stockMovementItem.populateLotQuantitiesAndCalculateNewSOH(totalLotMovementViewModelList,
                stockMovementItem.getMovementType());
        }
        return stockMovementItem;
    }

    public boolean validateEmpty() {
        return reason != null && StringUtils.isNoneEmpty(movementDate) && !allQuantitiesEmpty();
    }

    public boolean validateInputValid() {
        return (isAnyQuantitiesNumeric() && Long.parseLong(stockExistence) >= 0);
    }

    public boolean validateQuantitiesNotZero() {
        if (!StringUtils.isEmpty(getReceived())) {
            return Long.parseLong(getReceived()) > 0;
        } else if (!StringUtils.isEmpty(getIssued())) {
            return Long.parseLong(getIssued()) > 0;
        } else if (!StringUtils.isEmpty(getPositiveAdjustment())) {
            return Long.parseLong(getPositiveAdjustment()) > 0;
        } else if (!StringUtils.isEmpty(getNegativeAdjustment())) {
            return Long.parseLong(getNegativeAdjustment()) > 0;
        }
        return true;
    }

    public boolean isIssuedReason() {
        return getReason() != null && getReason().isIssueAdjustment();
    }

    private boolean allQuantitiesEmpty() {
        return StringUtils.isEmpty(getReceived())
                && StringUtils.isEmpty(getIssued())
                && StringUtils.isEmpty(getPositiveAdjustment())
                && StringUtils.isEmpty(getNegativeAdjustment());
    }

    private boolean isAnyQuantitiesNumeric() {
        return StringUtils.isNumeric(getReceived())
                || StringUtils.isNumeric(getNegativeAdjustment())
                || StringUtils.isNumeric(getPositiveAdjustment())
                || StringUtils.isNumeric(getIssued());
    }


    public void populateStockExistence(long previousStockOnHand) {
        if (!isKit) {
            this.stockExistence = "" + previousStockOnHand;
        } else {
            MovementReasonManager.MovementType movementType = typeQuantityMap.keySet().iterator().next();
            if (MovementReasonManager.MovementType.RECEIVE.equals(movementType) || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(movementType)) {
                this.stockExistence = "" + (previousStockOnHand + Long.parseLong(typeQuantityMap.get(movementType)));
            } else {
                this.stockExistence = "" + (previousStockOnHand - Long.parseLong(typeQuantityMap.get(movementType)));
            }
        }
    }

    public boolean movementQuantitiesExist() {
        for (LotMovementViewModel lot : existingLotMovementViewModelList) {
            if (lot.quantityGreaterThanZero()) return true;
        }
        return !newLotMovementViewModelList.isEmpty();
    }

    public boolean isLotEmpty() {
        return newLotMovementViewModelList.isEmpty() && existingLotMovementViewModelList.isEmpty();
    }

    public LotStatus getSoonestToExpireLotsIssued() {
        boolean soonestToExpireLotsIssued = true;
        boolean containExpiredLot = false;

        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (!containExpiredLot) {
                containExpiredLot = lotMovementViewModel.isExpiredLot();
            }
            if (!StringUtils.isEmpty(lotMovementViewModel.getQuantity()) && Long.parseLong(lotMovementViewModel.getQuantity()) > 0) {
                if (!soonestToExpireLotsIssued) {
                    return LotStatus.notSoonestToExpireLotsIssued;
                }
                if (Long.parseLong(lotMovementViewModel.getQuantity()) < Long.parseLong(lotMovementViewModel.getLotSoh())) {
                    soonestToExpireLotsIssued = false;
                }
            } else {
                if (containExpiredLot) {
                    return LotStatus.containExpiredLots;
                }
                soonestToExpireLotsIssued = false;
            }
        }
        return LotStatus.defaultStatus;
    }

    public boolean hasLotDataChanged() {
        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (lotMovementViewModel.isDataChanged) {
                return true;
            }
        }
        return !newLotMovementViewModelList.isEmpty();
    }

    public boolean validateSignature() {
        return signature.length() >= 2 && signature.matches("\\D+");
    }

    public boolean validateMovementReason() {
        return reason != null;
    }
}
