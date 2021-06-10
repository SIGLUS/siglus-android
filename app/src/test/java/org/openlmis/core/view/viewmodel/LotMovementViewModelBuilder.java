package org.openlmis.core.view.viewmodel;

import org.openlmis.core.manager.MovementReasonManager;

public class LotMovementViewModelBuilder {

  private final LotMovementViewModel viewModel;

  private MovementReasonManager.MovementType movementType;

  public LotMovementViewModelBuilder() {
    viewModel = new LotMovementViewModel();
  }

  public LotMovementViewModelBuilder setLotSOH(String soh) {
    viewModel.setLotSoh(soh);
    return this;
  }

  public LotMovementViewModelBuilder setLotNumber(String lotNumber) {
    viewModel.setLotNumber(lotNumber);
    return this;
  }

  public LotMovementViewModelBuilder setExpiryDate(String expiryDate) {
    viewModel.setExpiryDate(expiryDate);
    return this;
  }

  public LotMovementViewModelBuilder setMovementType(
      MovementReasonManager.MovementType movementType) {
    viewModel.setMovementType(movementType);
    return this;
  }

  public LotMovementViewModelBuilder setQuantity(String quantity) {
    viewModel.setQuantity(quantity);
    return this;
  }

  public LotMovementViewModel build() {
    return viewModel;
  }

  public LotMovementViewModelBuilder setHasLotDataChanged(boolean changedStatus) {
    viewModel.setDataChanged(changedStatus);
    return this;
  }
}
