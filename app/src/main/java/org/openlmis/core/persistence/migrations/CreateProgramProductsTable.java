package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProgramProductsTable extends Migration {

    @Override
    public void up() {
        execSQL("CREATE TABLE `program_products` (`programCode` VARCHAR, `productCode` VARCHAR, `isActive` BOOLEAN DEFAULT 1, `id` INTEGER PRIMARY KEY AUTOINCREMENT, unique(productCode, programCode));");
        execSQL("INSERT INTO program_products(programCode, productCode) select program.programCode programCode, products.code productCode from products join program where products.program_id = program.id;");
    }
}