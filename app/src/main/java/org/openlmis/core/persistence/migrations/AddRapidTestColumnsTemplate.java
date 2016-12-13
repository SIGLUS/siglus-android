package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class AddRapidTestColumnsTemplate extends Migration {

    @Override
    public void up() {

        String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);
        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'CONSUME_HIVDETERMINE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'POSITIVE_HIVDETERMINE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'CONSUME_HIVUNIGOLD', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'POSITIVE_HIVUNIGOLD', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'CONSUME_SYPHYLLIS', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'POSITIVE_SYPHYLLIS', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'CONSUME_MALARIA', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'POSITIVE_MALARIA', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");
    }
}
