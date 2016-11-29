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
                "'HIV-DETERMINE-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'HIV-DETERMINE-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'HIV-UNIGOLD-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'HIV-UNIGOLD-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'SYPHILLIS-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'SYPHILLIS-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'MALARIA-CONSUME', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");

        execSQL("INSERT INTO program_data_columns (code, label, description, program_id, createdAt, updatedAt) "
                + "VALUES (" +
                "'MALARIA-POSITIVE', " +
                "'', " +
                "'', " +
                "(SELECT id FROM programs WHERE programCode = 'RAPID_TEST'), " +
                "'" + formatDate + "', " +
                "'" + formatDate + "')");
    }
}
