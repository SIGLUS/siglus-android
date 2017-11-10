package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;


public class AddMalariaSignature extends Migration {

    @Override
    public void up() {
        execSQL("ALTER TABLE malaria_program ADD COLUMN createdBy CHAR(25)");
        execSQL("ALTER TABLE malaria_program ADD COLUMN verifiedBy CHAR(25)");
    }
}