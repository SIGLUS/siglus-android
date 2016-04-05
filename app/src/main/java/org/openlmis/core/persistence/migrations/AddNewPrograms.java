package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class AddNewPrograms extends Migration {

    @Override
    public void up() {
        String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt) " +
                "SELECT 'VIA', 'VIA', '" + formatDate + "' , '"  + formatDate + "' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'VIA')");

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt, parentCode) " +
                "SELECT 'TEST_KIT', 'Testes Rápidos Diag', '"  + formatDate + "' , '" + formatDate +   "', 'VIA' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'TEST_KIT')");

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt, parentCode) " +
                "SELECT 'TB', 'Tuberculose', '" + formatDate + "' , '" + formatDate +  "', 'VIA' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'TB')");

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt, parentCode) " +
                "SELECT 'MALARIA', 'Malaria', '" + formatDate + "' , '" + formatDate +  "', 'VIA' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'MALARIA')");

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt, parentCode) " +
                "SELECT 'PTV', 'PTV', '" + formatDate + "' , '" + formatDate + "', 'MMIA' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'PTV')");

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt, parentCode) " +
                "SELECT 'TARV', 'TARV', '" + formatDate + "' , '" + formatDate + "', 'MMIA' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'TARV')");

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt, parentCode) " +
                "SELECT 'PME', 'PME', '" + formatDate + "' , '" + formatDate + "', 'VIA' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'PME')");

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt, parentCode) " +
                "SELECT 'NUTRITION', 'NUTRITION', '" + formatDate + "' , '" + formatDate + "', 'VIA' " +
                "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'NUTRITION')");
    }
}