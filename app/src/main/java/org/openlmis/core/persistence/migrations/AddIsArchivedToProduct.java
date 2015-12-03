package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsArchivedToProduct extends Migration {

    @Override
    public void up() {
        execSQL("ALTER TABLE 'products' ADD COLUMN isArchived BOOLEAN DEFAULT false");
    }
}
