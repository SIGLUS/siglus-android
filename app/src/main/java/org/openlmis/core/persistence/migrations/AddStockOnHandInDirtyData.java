package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddStockOnHandInDirtyData extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE `dirty_data` ADD COLUMN stockOnHand BIGINT");
  }
}
