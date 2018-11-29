package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class CreateReportTypeTable extends Migration {
    @Override
    public void up() {
        execSQL("CREATE TABLE `reports_type` "
                + "(`code` VARCHAR, "
                + "`name` VARCHAR, "
                + "`description` VARCHAR, "
                + "`active` BOOLEAN, "
                + "`inActiveTime` VARCHAR NOT NULL, "
                + "`createdAt` VARCHAR NOT NULL, "
                + "`updatedAt` VARCHAR NOT NULL)");

    }
}
