package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsInitInventoryToStockMovementTable extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'stock_items' ADD COLUMN isInitInventory BOOLEAN DEFAULT 0");
  }
}
