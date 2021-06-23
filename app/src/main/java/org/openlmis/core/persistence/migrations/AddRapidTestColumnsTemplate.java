package org.openlmis.core.persistence.migrations;

import static org.openlmis.core.utils.DateUtil.DATE_TIME_FORMAT;

import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

public class AddRapidTestColumnsTemplate extends Migration {

  @Override
  public void up() {
    executeSQL("CONSUME_HIVDETERMINE");
    executeSQL("POSITIVE_HIVDETERMINE");
    executeSQL("CONSUME_HIVUNIGOLD");
    executeSQL("POSITIVE_HIVUNIGOLD");
    executeSQL("CONSUME_SYPHILLIS");
    executeSQL("POSITIVE_SYPHILLIS");
    executeSQL("CONSUME_MALARIA");
    executeSQL("POSITIVE_MALARIA");
  }

  private void executeSQL(String code) {
    String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DATE_TIME_FORMAT);
    execSQL(
        "INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
            + "VALUES ('" + code + "', '', '', "
            + "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), "
            + "'" + formatDate + "', "
            + "'" + formatDate + "')");
  }

}
