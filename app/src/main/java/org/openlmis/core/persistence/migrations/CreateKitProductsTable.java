package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class CreateKitProductsTable extends Migration {

    @Override
    public void up() {
        execSQL("CREATE TABLE `kit_products` (`kit_id` BIGINT, `product_id` BIGINT, `quantity` BIGINT ,`createdAt` VARCHAR NOT NULL, `updatedAt` VARCHAR NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)");
        execSQL("CREATE UNIQUE INDEX `kit_products_id_idx` ON `kit_products` ( `id` )");
    }
}
