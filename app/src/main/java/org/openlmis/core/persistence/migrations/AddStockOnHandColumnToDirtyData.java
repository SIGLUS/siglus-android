package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddStockOnHandColumnToDirtyData extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'dirty_data' ADD COLUMN stockOnHand BIGINT");
  }
}
