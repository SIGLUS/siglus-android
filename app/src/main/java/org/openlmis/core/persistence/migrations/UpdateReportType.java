package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateReportType extends Migration {

    @Override
    public void up() {
        String defaultReportTime = "2018-01-01 00:00:00.000000";
        execSQL("INSERT OR IGNORE INTO reports_type(code, name, description, active, startTime, createdAt, updatedAt) " +
                "SELECT programs.programCode, 'MMIA', 'MMIA', '1',' " + defaultReportTime + "', programs.createdAt, programs.updatedAt " +
                "FROM programs " +
                "WHERE programs.programCode = 'MMIA' " +
                "AND NOT EXISTS (SELECT * FROM reports_type WHERE code = 'MMIA');");

        execSQL("INSERT OR IGNORE INTO reports_type(code, name, description, active, startTime, createdAt, updatedAt) " +
                "SELECT programs.programCode, 'RAPID TEST', 'RAPID TEST', '1',' " + defaultReportTime + "', programs.createdAt, programs.updatedAt " +
                "FROM programs " +
                "WHERE programs.programCode = 'TEST_KIT' " +
                "AND NOT EXISTS (SELECT * FROM reports_type WHERE code = 'TEST_KIT');");

        execSQL("INSERT OR IGNORE INTO reports_type(code, name, description, active, startTime, createdAt, updatedAt) " +
                "SELECT programs.programCode, 'Balance Requisition', 'Balance Requisition', '1',' " + defaultReportTime + "', programs.createdAt, programs.updatedAt " +
                "FROM programs " +
                "WHERE programs.programCode = 'VIA' " +
                "AND NOT EXISTS (SELECT * FROM reports_type WHERE code = 'VIA');");

    }
}
