package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddRegimePharmacy extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'regime_items' ADD COLUMN pharmacy BIGINT DEFAULT 0");
  }
}
