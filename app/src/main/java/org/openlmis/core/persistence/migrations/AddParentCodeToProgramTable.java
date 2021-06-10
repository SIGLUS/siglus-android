package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddParentCodeToProgramTable extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'program' ADD COLUMN parentCode VARCHAR");
  }
}
