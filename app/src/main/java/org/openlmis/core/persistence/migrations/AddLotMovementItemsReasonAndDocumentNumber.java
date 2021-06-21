package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddLotMovementItemsReasonAndDocumentNumber extends Migration {

  @Override
  public void up() {
      execSQL("ALTER TABLE 'lot_movement_items' ADD COLUMN reason VARCHAR");
      execSQL("ALTER TABLE 'lot_movement_items' ADD COLUMN documentNumber VARCHAR");
  }
}
