package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateCategoryColumnForMMIAProducts extends Migration {

  private static final String WHERE_MMIA_PROGRAM = "WHERE programCode = 'MMIA' ";

  @Override
  public void up() {
    execSQL("UPDATE product_programs SET category = 'Adult'" +
        WHERE_MMIA_PROGRAM +
        "AND productCode IN ('08S42', '08S18Y', '08S40', '08S36', '08S32', '08S18Z', '08S39Z', '08S21', '08S01', '08S22', '08S13', '08S15') ");

    execSQL("UPDATE product_programs SET category = 'Children'" +
        WHERE_MMIA_PROGRAM +
        "AND productCode IN ('08S34B', '08S32Z', '08S42B', '08S40Z', '08S39B', '08S39Y', '08S01ZZ', '08S20', '08S19', '08S01B') ");

    execSQL("UPDATE product_programs SET category = 'Solution'" +
        WHERE_MMIA_PROGRAM +
        "AND productCode IN ('08S23', '08S17') ");
  }
}
