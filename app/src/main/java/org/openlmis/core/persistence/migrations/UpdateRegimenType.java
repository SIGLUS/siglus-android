package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateRegimenType extends Migration {

  @Override
  public void up() {
    execSQL("UPDATE regimes SET type = 'Adults' where type = 'ADULT' ");
    execSQL("UPDATE regimes SET type = 'Paediatrics' where type = 'BABY' ");
  }
}
