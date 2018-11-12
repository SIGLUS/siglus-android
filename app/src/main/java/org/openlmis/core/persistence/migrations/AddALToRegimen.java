package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddALToRegimen extends Migration {
    @Override
    public void up() {
        execSQL("ALTER TABLE 'RegimenItem' ADD COLUMN hf BIGINT");
        execSQL("ALTER TABLE 'RegimenItem' ADD COLUMN chw BIGINT");
    }
}
