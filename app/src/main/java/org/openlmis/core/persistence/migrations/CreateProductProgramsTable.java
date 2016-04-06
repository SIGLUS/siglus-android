package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateProductProgramsTable extends Migration {

    @Override
    public void up() {
        execSQL("CREATE TABLE `product_programs` (`programCode` VARCHAR, `productCode` VARCHAR, `isActive` BOOLEAN DEFAULT 1, `id` INTEGER PRIMARY KEY AUTOINCREMENT, unique(productCode, programCode));");
        execSQL("INSERT INTO product_programs(programCode, productCode) select program.programCode programCode, products.code productCode from products join program where products.program_id = program.id;");
    }
}