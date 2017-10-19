package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateDepartmentTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `department` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`name` VARCHAR NOT NULL);");

        insertData();
    }

    private void insertData(){
        execSQL("INSERT INTO department (`id`, `name`) values ('1', 'CPN');");
        execSQL("INSERT INTO department (`id`, `name`) values ('2', 'Maternity');");
        execSQL("INSERT INTO department (`id`, `name`) values ('3', 'CCR');");
        execSQL("INSERT INTO department (`id`, `name`) values ('4', 'Pharmacy');");
    }
}
