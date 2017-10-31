package org.openlmis.core.utils.mapper;

import com.google.inject.Inject;

import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.view.viewmodel.malaria.MalariaDataReportViewModel;

import java.lang.reflect.InvocationTargetException;

public class MalariaProgramToMalariaDataReportViewModelMapper {

    @Inject
    private ImplementationListToImplementationReportViewModelMapper implementationReportMapper;

    public MalariaDataReportViewModel Map(MalariaProgram malariaProgram) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        if (malariaProgram == null) {
            return new MalariaDataReportViewModel();
        }
        return new MalariaDataReportViewModel(malariaProgram.getReportedDate(),
                malariaProgram.getStartPeriodDate(),
                malariaProgram.getEndPeriodDate(),
                implementationReportMapper.mapUsImplementations(malariaProgram.getImplementations(), malariaProgram.getStatus()),
                implementationReportMapper.mapApeImplementations(malariaProgram.getImplementations(), malariaProgram.getStatus()));

    }
}
