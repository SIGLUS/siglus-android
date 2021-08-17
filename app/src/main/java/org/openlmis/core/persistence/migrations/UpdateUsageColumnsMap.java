package org.openlmis.core.persistence.migrations;

import java.util.Arrays;
import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

public class UpdateUsageColumnsMap extends Migration {

  private static final String UNJUSTIFIED = "UNJUSTIFIED";
  private static final String CONSUME = "CONSUME";
  private static final String POSITIVE = "POSITIVE";
  private static final String INSERT_INTO_USAGE_COLUMNS_MAP = "INSERT INTO usage_columns_map (code,"
      + " testOutcome, testProject, createdAt, updatedAt) ";
  private static final String VALUES = "VALUES (";
  private static final String FORMAT_DATE = DateUtil
      .formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);

  public void up() {
    for (String testOutcome : Arrays.asList(UNJUSTIFIED, CONSUME, POSITIVE)) {
      executeSQL(testOutcome, "HIVDETERMINE");
      executeSQL(testOutcome, "HIVUNIGOLD");
      executeSQL(testOutcome, "SYPHILLIS");
      executeSQL(testOutcome, "MALARIA");
    }
  }

  private void executeSQL(String testOutcome, String testProject) {
    String usageSqlCode = "'" + testOutcome.concat("_").concat(testProject) + "',";
    String usageSqlOutcome = "'" + testOutcome + "',";
    String usageSqlProject = "'" + testProject + "',";
    execSQL(
        INSERT_INTO_USAGE_COLUMNS_MAP + VALUES + usageSqlCode + usageSqlOutcome + usageSqlProject
            + "'" + FORMAT_DATE + "', " + "'" + FORMAT_DATE + "')");
  }
}
