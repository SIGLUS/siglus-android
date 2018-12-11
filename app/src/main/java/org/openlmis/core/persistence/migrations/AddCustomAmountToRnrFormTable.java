package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddCustomAmountToRnrFormTable extends Migration {
    @Override
    public void up() {
        execSQL("ALTER TABLE 'rnr_form_items' ADD COLUMN isCustomAmount BOOLEAN DEFAULT 0");
    }
}
