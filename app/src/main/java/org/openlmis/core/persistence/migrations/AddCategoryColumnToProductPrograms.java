package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddCategoryColumnToProductPrograms extends Migration {
    @Override
    public void up() {
        execSQL("ALTER TABLE 'product_programs' ADD COLUMN category DEFAULT 'Other'");
    }
}
