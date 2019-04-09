package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateProgramDataFormTable extends Migration{
    @Override
    public void up() {
        execSQL("ALTER TABLE 'program_data_forms' ADD COLUMN observation VARCHAR DEFAULT ''");
    }
}
