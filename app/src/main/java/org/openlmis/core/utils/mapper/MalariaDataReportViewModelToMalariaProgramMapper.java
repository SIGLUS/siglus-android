package org.openlmis.core.utils.mapper;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.view.viewmodel.malaria.MalariaDataReportViewModel;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import static org.joda.time.DateTime.now;

public class MalariaDataReportViewModelToMalariaProgramMapper {

    @Inject
    private ImplementationReportViewModelToImplementationListMapper implementationsMapper;

    public MalariaProgram map(MalariaDataReportViewModel malariaDataReportViewModel, MalariaProgram malariaProgram) throws LMISException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (malariaProgram == null) {
            return generateMalariaProgramFromScratch(malariaDataReportViewModel);
        }
        return generateMalariaProgramFrom(malariaDataReportViewModel, malariaProgram);
    }

    private MalariaProgram generateMalariaProgramFromScratch(MalariaDataReportViewModel malariaDataReportViewModel) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, LMISException {
        String username = UserInfoMgr.getInstance().getUser().getUsername();
        DateTime today = now();
        Collection<Implementation> implementations = implementationsMapper.map(
                malariaDataReportViewModel.getUsImplementationReportViewModel(),
                malariaDataReportViewModel.getApeImplementationReportViewModel());
        return new MalariaProgram(username, today,
                malariaDataReportViewModel.getStartPeriodDate(),
                malariaDataReportViewModel.getEndPeriodDate(),
                implementations);
    }

    private MalariaProgram generateMalariaProgramFrom(MalariaDataReportViewModel malariaDataReportViewModel, MalariaProgram malariaProgram) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String username = UserInfoMgr.getInstance().getUser().getUsername();
        DateTime today = now();
        Collection<Implementation> implementations = implementationsMapper.map(
                malariaDataReportViewModel.getUsImplementationReportViewModel(),
                malariaProgram.getImplementations());
        implementations = implementationsMapper.map(
                malariaDataReportViewModel.getApeImplementationReportViewModel(), implementations);
        malariaProgram.setEndPeriodDate(malariaDataReportViewModel.getEndPeriodDate());
        malariaProgram.setStartPeriodDate(malariaDataReportViewModel.getStartPeriodDate());
        malariaProgram.setUsername(username);
        malariaProgram.setReportedDate(today);
        malariaProgram.setImplementations(implementations);
        return malariaProgram;
    }

}
