package org.openlmis.core.persistence.migrations;

import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

public class UpdateRapidTestColumnsTemplate extends Migration {

  private static final String INSERT_INTO_PROGRAM_DATA_COLUMNS = "INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) ";
  private static final String VALUES = "VALUES (";
  private static final String SELECT_ID = "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), ";
  private static final String SPLIT = "'', ";

  @Override
  public void up() {
    String formatDate = DateUtil
        .formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);
    String createAt = "'" + formatDate + "', ";
    String updateAt = "'" + formatDate + "')";
    execSQL(
        INSERT_INTO_PROGRAM_DATA_COLUMNS + VALUES + "'UNJUSTIFIED_HIVDETERMINE', " + SPLIT + SPLIT + SELECT_ID
            + createAt + updateAt);
    execSQL(
        INSERT_INTO_PROGRAM_DATA_COLUMNS + VALUES + "'UNJUSTIFIED_HIVUNIGOLD', " + SPLIT + SPLIT + SELECT_ID + createAt
            + updateAt);
    execSQL(
        INSERT_INTO_PROGRAM_DATA_COLUMNS + VALUES + "'UNJUSTIFIED_SYPHILLIS', " + SPLIT + SPLIT + SELECT_ID + createAt
            + updateAt);
    execSQL(
        INSERT_INTO_PROGRAM_DATA_COLUMNS + VALUES + "'UNJUSTIFIED_MALARIA', " + SPLIT + SPLIT + SELECT_ID + createAt
            + updateAt);
  }
}
