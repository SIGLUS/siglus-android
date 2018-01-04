package org.openlmis.core.helpers;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import org.joda.time.DateTime;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.ViaReportStatus;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.joda.time.DateTime.now;
import static org.openlmis.core.helpers.ImplementationBuilder.createDefaultImplementations;
import static org.openlmis.core.helpers.ImplementationBuilder.randomImplementation;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class MalariaProgramBuilder {
    public static final Property<MalariaProgram, String> username = new Property<>();
    public static final Property<MalariaProgram, DateTime> reportedDate = new Property<>();
    public static final Property<MalariaProgram, DateTime> periodStartDate = new Property<>();
    public static final Property<MalariaProgram, DateTime> periodEndDate = new Property<>();
    public static final Property<MalariaProgram, ViaReportStatus> status = new Property<>();
    public static final Property<MalariaProgram, List<Implementation>> implementations = new Property<>();

    public static final Instantiator<MalariaProgram> randomMalariaProgram = new Instantiator<MalariaProgram>() {
        @Override
        public MalariaProgram instantiate(PropertyLookup<MalariaProgram> lookup) {
            DateTime referenceDate = now();
            ViaReportStatus[] statuses = ViaReportStatus.values();
            MalariaProgram malariaProgram = new MalariaProgram(
                    lookup.valueOf(username, randomAlphabetic(10)),
                    lookup.valueOf(reportedDate, referenceDate),
                    lookup.valueOf(periodStartDate, referenceDate.minusDays(nextInt(10))),
                    lookup.valueOf(periodEndDate, referenceDate.plusDays(nextInt(10))),
                    lookup.valueOf(implementations, newArrayList(make(a(randomImplementation)), make(a(randomImplementation)))));
            malariaProgram.setStatus(lookup.valueOf(status, statuses[nextInt(statuses.length)]));
            return malariaProgram;
        }
    };

    public static final Instantiator<MalariaProgram> defaultMalariaProgram = new Instantiator<MalariaProgram>() {
        @Override
        public MalariaProgram instantiate(PropertyLookup<MalariaProgram> lookup) {
            DateTime referenceDate = now();
            ViaReportStatus[] statuses = ViaReportStatus.values();
            MalariaProgram malariaProgram = new MalariaProgram(
                    lookup.valueOf(username, randomAlphabetic(10)),
                    lookup.valueOf(reportedDate, referenceDate),
                    lookup.valueOf(periodStartDate, referenceDate.minusDays(nextInt(10))),
                    lookup.valueOf(periodEndDate, referenceDate.plusDays(nextInt(10))),
                    lookup.valueOf(implementations, createDefaultImplementations()));
            malariaProgram.setStatus(lookup.valueOf(status, statuses[nextInt(statuses.length)]));
            return malariaProgram;
        }
    };
}

