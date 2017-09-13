package org.openlmis.core.service;

import org.joda.time.DateTime;
import org.openlmis.core.model.Period;

import java.util.List;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class PatientDataService {
    public List<Period> calculatePeriods() {
        return newArrayList(new Period(DateTime.now()));
    }
}
