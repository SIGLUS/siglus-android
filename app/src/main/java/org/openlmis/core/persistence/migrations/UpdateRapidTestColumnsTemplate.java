package org.openlmis.core.persistence.migrations;

import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

public class UpdateRapidTestColumnsTemplate extends Migration {

  @Override
  public void up() {
    String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()),
        DateUtil.DATE_TIME_FORMAT);
    execSQL(
        "INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
            + "VALUES (" +
            "'UNJUSTIFIED_HIVDETERMINE', " +
            "'', " +
            "'', " +
            "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
            "'" + formatDate + "', " +
            "'" + formatDate + "')");
    execSQL(
        "INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
            + "VALUES (" +
            "'UNJUSTIFIED_HIVUNIGOLD', " +
            "'', " +
            "'', " +
            "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
            "'" + formatDate + "', " +
            "'" + formatDate + "')");
    execSQL(
        "INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
            + "VALUES (" +
            "'UNJUSTIFIED_SYPHILLIS', " +
            "'', " +
            "'', " +
            "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
            "'" + formatDate + "', " +
            "'" + formatDate + "')");

    execSQL(
        "INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
            + "VALUES (" +
            "'UNJUSTIFIED_MALARIA', " +
            "'', " +
            "'', " +
            "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
            "'" + formatDate + "', " +
            "'" + formatDate + "')");
  }
}
