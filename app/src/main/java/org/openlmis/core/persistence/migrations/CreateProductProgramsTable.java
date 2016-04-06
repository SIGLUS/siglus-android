package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProductProgramsTable extends Migration {

    @Override
    public void up() {
        execSQL("CREATE TABLE `product_programs` (`programCode` VARCHAR, `productCode` VARCHAR, `isActive` BOOLEAN DEFAULT 1, `id` INTEGER PRIMARY KEY AUTOINCREMENT,  `createdAt` VARCHAR NOT NULL , `updatedAt` VARCHAR NOT NULL, unique(productCode, programCode));");
        execSQL("INSERT INTO product_programs(programCode, productCode, createdAt, updatedAt) "
                + "select programs.programCode programCode, products.code productCode, products.createdAt createdAt, products.updatedAt updatedAt "
                + "from products join programs where products.program_id = programs.id;");
    }
}