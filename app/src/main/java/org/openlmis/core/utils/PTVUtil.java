package org.openlmis.core.utils;

import org.joda.time.DateTime;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.Period;

import java.util.Date;

public final class PTVUtil {

    private PTVUtil() {
    }

    public static PTVProgram createDummyPTVProgram() {
        DateTime today = DateTime.now();
        Period period = new Period(today);
        Date startPeriod = period.getBegin().toDate();
        Date endPeriod = period.getEnd().toDate();
        PTVProgram ptvProgramExpected = new PTVProgram();
        ptvProgramExpected.setStartPeriod(startPeriod);
        ptvProgramExpected.setEndPeriod(endPeriod);
        ptvProgramExpected.setCreatedBy("TWUIO");
        ptvProgramExpected.setVerifiedBy("MZ");
        ptvProgramExpected.setCreatedAt(today.toDate());
        ptvProgramExpected.setUpdatedAt(today.toDate());
        return ptvProgramExpected;
    }
}
