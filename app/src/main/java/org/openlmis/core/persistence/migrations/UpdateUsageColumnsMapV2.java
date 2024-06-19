package org.openlmis.core.persistence.migrations;

import java.util.Arrays;

public class UpdateUsageColumnsMapV2 extends UpdateUsageColumnsMap {

  public static final String POSITIVE_HIV = "POSITIVEHIV";
  public static final String POSITIVE_SYPHILIS = "POSITIVESYPHILIS";

  @Override
  public void up() {
    for (String testOutcome : Arrays.asList(UNJUSTIFIED, CONSUME, POSITIVE)) {
      executeSQL(testOutcome, "HEPATITEBTESTES");
      executeSQL(testOutcome, "TDRORALDEHIV");
      executeSQL(testOutcome, "NEWTEST");
    }

    for (String testOutcome : Arrays.asList(
        UNJUSTIFIED, CONSUME, POSITIVE_HIV, POSITIVE_SYPHILIS)) {
      executeSQL(testOutcome, "DUOTESTEHIVSIFILIS");
    }
  }
}