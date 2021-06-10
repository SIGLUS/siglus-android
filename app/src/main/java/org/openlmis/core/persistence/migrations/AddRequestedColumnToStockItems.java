package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddRequestedColumnToStockItems extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'stock_items' ADD COLUMN requested INTEGER");
  }
}
