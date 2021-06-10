package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddRnrBaseInfoItem extends Migration {

  @Override
  public void up() {
    execSQL("ALTER TABLE 'rnr_baseInfo_items' ADD COLUMN tableName VARCHAR");
    execSQL("ALTER TABLE 'rnr_baseInfo_items' ADD COLUMN displayOrder INTEGER");
  }
}
