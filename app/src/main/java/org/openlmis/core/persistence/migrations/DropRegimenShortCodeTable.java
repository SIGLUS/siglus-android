package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class DropRegimenShortCodeTable extends Migration {

  @Override
  public void up() {
    execSQL("DROP TABLE `regime_short_code`;");
  }
}
