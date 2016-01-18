package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateProductsFalseValueToZero extends Migration {

    @Override
    public void up() {
        execSQL("UPDATE products SET isKit = '0' WHERE isKit ='false'");
        execSQL("UPDATE products SET isArchived = '0' WHERE isArchived = 'false'");
    }
}
