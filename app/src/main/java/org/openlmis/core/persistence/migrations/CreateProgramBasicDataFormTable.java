package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramBasicDataFormTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `program_data_Basic_items` " +
                "(`code` VARCHAR , " +
                "`form_id` BIGINT , " +
                "`name` VARCHAR , " +
                "`initialAmount` BIGINT , " +
                "`received` BIGINT , " +
                "`issued` BIGINT , " +
                "`adjustment` BIGINT , " +
                "`validate` VARCHAR , " +
                "`inventory` BIGINT  )");
    }
}
