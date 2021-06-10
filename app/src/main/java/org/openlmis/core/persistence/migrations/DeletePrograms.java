package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class DeletePrograms extends Migration {

  @Override
  public void up() {
    execSQL("DELETE FROM programs");
  }
}
