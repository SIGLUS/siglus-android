package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddVersionCodeToProgramProductTable extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'product_programs' ADD COLUMN versionCode INTEGER DEFAULT 0");
  }
}
