package org.openlmis.core.view.viewmodel;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;

@Data
public class BaseStockMovementViewModel {

  Product product;

  MovementReasonManager.MovementType movementType;

  List<LotMovementViewModel> newLotMovementViewModelList = new ArrayList<>();
  List<LotMovementViewModel> existingLotMovementViewModelList = new ArrayList<>();
}
