package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddProductPrice extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'products' ADD COLUMN price double");
  }
}
