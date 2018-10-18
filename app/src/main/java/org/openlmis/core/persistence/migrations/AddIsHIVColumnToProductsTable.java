package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsHIVColumnToProductsTable extends Migration {
    @Override
    public void up() {
        execSQL("ALTER TABLE 'products' ADD COLUMN isHiv BOOLEAN DEFAULT 0");
    }
}

