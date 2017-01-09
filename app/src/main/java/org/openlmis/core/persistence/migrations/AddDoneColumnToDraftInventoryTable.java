package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddDoneColumnToDraftInventoryTable extends Migration {
    @Override
    public void up() {
        execSQL("ALTER TABLE 'draft_inventory' ADD COLUMN done BOOLEAN DEFAULT 0");
    }
}
