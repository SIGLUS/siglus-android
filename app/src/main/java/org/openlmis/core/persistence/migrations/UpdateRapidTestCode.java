package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateRapidTestCode extends Migration {

  @Override
  public void up() {
    // 后端代码将rapidTest 迁移到 TEST_KIT
    execSQL(
        "UPDATE program_data_columns SET program_id = (SELECT id FROM programs WHERE programCode = 'TEST_KIT') "
            + "WHERE program_id = (SELECT id FROM programs WHERE programCode = 'RAPID_TEST')");
    execSQL(
        "UPDATE program_data_forms SET program_id = (SELECT id FROM programs WHERE programCode = 'TEST_KIT') "
            + "WHERE program_id = (SELECT id FROM programs WHERE programCode = 'RAPID_TEST')");
  }
}
