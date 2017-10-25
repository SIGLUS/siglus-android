package org.openlmis.core.utils;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.Period;

import java.util.ArrayList;
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

    @NonNull
    public static ArrayList<HealthFacilityService> createDummyHealthFacilityServices() {
        ArrayList<HealthFacilityService> expectedHealthFacilityServices = new ArrayList<>();
        expectedHealthFacilityServices.add(getHealthFacilityService(1,"CPN"));
        expectedHealthFacilityServices.add(getHealthFacilityService(2,"Maternity"));
        expectedHealthFacilityServices.add(getHealthFacilityService(3,"CCR"));
        expectedHealthFacilityServices.add(getHealthFacilityService(4,"Pharmacy"));
        expectedHealthFacilityServices.add(getHealthFacilityService(5,"UATS"));
        expectedHealthFacilityServices.add(getHealthFacilityService(6,"Banco de socorro"));
        expectedHealthFacilityServices.add(getHealthFacilityService(7,"Lab"));
        expectedHealthFacilityServices.add(getHealthFacilityService(8,"Estomatologia"));
        return expectedHealthFacilityServices;
    }

    @NonNull
    private static HealthFacilityService getHealthFacilityService(int id, String name) {
        HealthFacilityService healthFacilityService = new HealthFacilityService();
        healthFacilityService.setId(id);
        healthFacilityService.setName(name);
        return healthFacilityService;
    }
}
