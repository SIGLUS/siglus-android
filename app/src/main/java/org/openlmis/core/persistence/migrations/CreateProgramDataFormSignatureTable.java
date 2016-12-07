package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramDataFormSignatureTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `program_data_form_signatures` (`form_id` BIGINT, `signature` VARCHAR, `type` VARCHAR ,`id` INTEGER PRIMARY KEY AUTOINCREMENT)");
    }
}
