package org.openlmis.core.model.builder;

import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.model.StockMovementItem.MovementType;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

public class StockMovementViewModelBuilder {

    String movementDate = "10/10/2010";
    String stockExistence = "200";
    String documentNo = "abc";
    MovementReason movementReason = new MovementReason(MovementType.ISSUE, "ISSUE_1", "");
    String issued = "12";
    String negativeAdjustment;
    String positiveAdjustment;
    String received;

    public StockMovementViewModel build() {
        StockMovementViewModel viewModel = new StockMovementViewModel();
        viewModel.setMovementDate(movementDate);
        viewModel.setDocumentNo(documentNo);
        viewModel.setStockExistence(stockExistence);
        viewModel.setReason(movementReason);
        viewModel.setIssued(issued);
        viewModel.setReceived(received);
        viewModel.setNegativeAdjustment(negativeAdjustment);
        viewModel.setPositiveAdjustment(positiveAdjustment);
        return viewModel;
    }

    public StockMovementViewModelBuilder withMovementDate(String movementDate) {
        this.movementDate = movementDate;
        return this;
    }

    public StockMovementViewModelBuilder withDocumentNo(String documentNo) {
        this.documentNo = documentNo;
        return this;
    }

    public StockMovementViewModelBuilder withMovementReason(MovementReason movementReason) {
        this.movementReason = movementReason;
        return this;
    }

    public StockMovementViewModelBuilder withIssued(String issued) {
        this.issued = issued;
        return this;
    }

    public StockMovementViewModelBuilder withReceived(String received) {
        this.received = received;
        return this;
    }

    public StockMovementViewModelBuilder withNegativeAdjustment(String negativeAdjustment) {
        this.negativeAdjustment = negativeAdjustment;
        return this;
    }

    public StockMovementViewModelBuilder withPositiveAdjustment(String positiveAdjustment) {
        this.positiveAdjustment = positiveAdjustment;
        return this;
    }

    public StockMovementViewModelBuilder withStockExistence(String stockExistence) {
        this.stockExistence = stockExistence;
        return this;
    }
}
