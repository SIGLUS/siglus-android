package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class AddRapidTestProgramFormTemplate extends Migration {

    @Override
    public void up() {
        execSQL("CREATE TABLE `program_data_columns` " +
                "(`code` VARCHAR NOT NULL, " +
                "`label` VARCHAR , " +
                "`description` VARCHAR , " +
                "`program_id` BIGINT, " +
                "`createdAt` VARCHAR NOT NULL , " +
                "`updatedAt` VARCHAR NOT NULL , " +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT )");

        String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);
        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'HIV-DETERMINE-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'HIV-DETERMINE-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'HIV-UNIGOLD-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'HIV-UNIGOLD-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'SYPHILLIS-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'SYPHILLIS-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'MALARIA-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'MALARIA-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE code = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");
    }
}
