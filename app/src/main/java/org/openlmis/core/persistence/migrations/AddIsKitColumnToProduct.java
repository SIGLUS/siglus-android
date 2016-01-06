package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsKitColumnToProduct extends Migration {

    @Override
    public void up() {
        execSQL("ALTER TABLE 'products' ADD COLUMN isKit BOOLEAN DEFAULT false");
    }
}
