package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateStockCardSOHStatus extends Migration {

  @Override
  public void up() {
    execSQL(
        "ALTER TABLE `stock_cards` ADD COLUMN stockOnHandStatus VARCHAR DEFAULT 'REGULAR_STOCK'");
  }
}
