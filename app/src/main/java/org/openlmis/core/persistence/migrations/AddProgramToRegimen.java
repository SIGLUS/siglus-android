package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddProgramToRegimen extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'regimes' ADD `program_id` BIGINT");
  }
}
