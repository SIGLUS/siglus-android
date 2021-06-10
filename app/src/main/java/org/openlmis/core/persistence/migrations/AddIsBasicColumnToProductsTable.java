package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddIsBasicColumnToProductsTable extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'products' ADD COLUMN isBasic BOOLEAN DEFAULT 0");
  }
}
