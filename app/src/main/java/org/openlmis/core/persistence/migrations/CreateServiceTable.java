package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateServiceTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `service` ("
                + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`name` VARCHAR NOT NULL, "
                + "`peripheral` INTEGER NOT NULL);");

        insertData();
    }

    private void insertData(){
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('1', 'CPN', '0');");
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('2', 'Maternity', '0');");
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('3', 'CCR', '0');");
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('4', 'Pharmacy', '0');");
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('5', 'UATS', '1');");
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('6', 'Banco de socorro', '1');");
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('7', 'Lab', '1');");
        execSQL("INSERT INTO service (`id`, `name`, `peripheral`) values ('8', 'Estomatologia', '1');");
    }
}
