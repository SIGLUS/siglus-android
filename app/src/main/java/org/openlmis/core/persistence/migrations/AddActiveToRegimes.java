package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddActiveToRegimes extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'regimes' ADD COLUMN active BOOLEAN");
  }
}
