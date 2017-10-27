package org.openlmis.core.builders;

import com.google.inject.Inject;

import org.openlmis.core.enums.PatientDataStatusEnum;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;

import java.util.List;

public class PTVProgramBuilder {

    public static final String EMPTY_STRING = "";
    @Inject
    PatientDispensationBuilder patientDispensationBuilder;

    @Inject
    PTVProgramStockInformationBuilder ptvProgramStockInformationBuilder;

    public PTVProgram buildPTVProgram(Period period) throws LMISException {
        PTVProgram ptvProgram = new PTVProgram();
        ptvProgram.setStartPeriod(period.getBegin().toDate());
        ptvProgram.setEndPeriod(period.getEnd().toDate());
        ptvProgram.setStatus(PatientDataStatusEnum.MISSING);
        ptvProgram.setCreatedBy(EMPTY_STRING);
        ptvProgram.setVerifiedBy(EMPTY_STRING);
        List<PatientDispensation> patientDispensations = patientDispensationBuilder.buildInitialPatientDispensations(ptvProgram);
        ptvProgram.setPatientDispensations(patientDispensations);
        List<PTVProgramStockInformation> ptvProgramStocksInformation = ptvProgramStockInformationBuilder.buildPTVProgramStockInformation(ptvProgram);
        ptvProgram.setPtvProgramStocksInformation(ptvProgramStocksInformation);
        return ptvProgram;
    }
}
