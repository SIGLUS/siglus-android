package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsAddedInPodProductLotItems extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE `pod_product_lot_items` ADD COLUMN isAdded BOOLEAN DEFAULT 0");
  }
}