package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

/*
 * This migration fix the wrong kit update incident.
 * full right kit product code is '26A01' '26A01' '26A02' '26B02'
 * */
public class UpdateKitProductUnSynced extends Migration {

  @Override
  public void up() {
    execSQL("UPDATE " +
        "    stock_items " +
        "SET " +
        "    synced = 0 " +
        "WHERE " +
        "    stockCard_id IN ( " +
        "        SELECT id " +
        "    FROM " +
        "        stock_cards " +
        "    WHERE " +
        "        product_id IN ( " +
        "            SELECT id " +
        "        FROM " +
        "            products " +
        "        WHERE " +
        "            code IN ('26A01', " +
        "            '26B01', " +
        "            '26A02', " +
        "            '26B02')))");
  }
}
