package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class ChangeProgramTableName extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE program RENAME TO programs;");
  }
}
