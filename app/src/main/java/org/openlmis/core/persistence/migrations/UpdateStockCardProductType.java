package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateStockCardProductType extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE stock_cards RENAME TO stock_cards2;");
    execSQL("CREATE TABLE `stock_cards` "
        + "(`expireDates` VARCHAR ,"
        + "`product_id` BIGINT UNIQUE, "
        + "`stockOnHand` BIGINT , "
        + "`createdAt` VARCHAR NOT NULL, "
        + "`updatedAt` VARCHAR NOT NULL, "
        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "`avgMonthlyConsumption` REAL);");
    execSQL("INSERT OR IGNORE INTO stock_cards " +
        "(expireDates, product_id, stockOnHand, createdAt, updatedAt, id, avgMonthlyConsumption) " +
        "SELECT expireDates, product_id, stockOnHand, createdAt, updatedAt, id, avgMonthlyConsumption from stock_cards2 ;");
    execSQL("DROP TABLE `stock_cards2`;");
  }
}
