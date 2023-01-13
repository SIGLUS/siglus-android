package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class DropProgramDataFormTables extends Migration {

  @Override
  public void up() {
    execSQL("DROP TABLE `program_data_columns`;");
    execSQL("DROP TABLE `program_data_Basic_items`;");
    execSQL("DROP TABLE `program_data_form_signatures`;");
    execSQL("DROP TABLE `program_data_items`;");
    execSQL("DROP TABLE `program_data_forms`;");
  }
}
