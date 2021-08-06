package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class AddRapidTestProgram extends Migration {
    @Override
    public void up() {
        String formatDate = DateUtil.formatDate(new Date(LMISApp.getInstance().getCurrentTimeMillis()), DateUtil.DATE_TIME_FORMAT);

        execSQL("INSERT INTO programs (programCode, programName, createdAt, updatedAt) "
                + "SELECT 'RAPID_TEST', 'Rapid Test', '" + formatDate + "' , '" + formatDate + "' "
                + "WHERE NOT EXISTS (SELECT * FROM programs WHERE programCode = 'RAPID_TEST')");
    }
}
