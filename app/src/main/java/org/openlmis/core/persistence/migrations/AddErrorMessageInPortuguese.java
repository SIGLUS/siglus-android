package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddErrorMessageInPortuguese extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'sync_errors' ADD COLUMN errorMessageInPortuguese VARCHAR");
  }
}
