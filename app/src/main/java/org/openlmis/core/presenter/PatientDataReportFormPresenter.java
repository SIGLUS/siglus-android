package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Period;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.PatientDataReportViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatientDataReportFormPresenter extends BaseReportPresenter {

    public static final int MALARIA_PRODUCTS_TOTAL = 4;
    @Inject
    PatientDataService patientDataService;

    private List<PatientDataReportViewModel> viewModels;

    public PatientDataReportFormPresenter() {
        this.viewModels = new ArrayList<>();
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public List<PatientDataReportViewModel> getViewModels() {
        generateViewModelsForAvailablePeriods();
        return viewModels;
    }

    public void generateViewModelsForAvailablePeriods() {
        viewModels.clear();
        Period period = new Period(DateTime.now());
        PatientDataReportViewModel patientDataReportViewModelUs = new PatientDataReportViewModel(period);
        PatientDataReportViewModel patientDataReportViewModelAPE = new PatientDataReportViewModel(period);
        PatientDataReportViewModel patientDataReportViewModelTotal = new PatientDataReportViewModel(period);
        patientDataReportViewModelUs.setUsApe("US");
        patientDataReportViewModelUs.setExistingStock(patientDataService.getMalariaProductsStockHand());
        patientDataReportViewModelUs.setCurrentTreatments(generateEmptyFields());
        patientDataReportViewModelAPE.setUsApe("APE");
        patientDataReportViewModelAPE.setExistingStock(generateEmptyFields());
        patientDataReportViewModelAPE.setCurrentTreatments(generateEmptyFields());
        patientDataReportViewModelTotal.setUsApe("TOTAL");
        patientDataReportViewModelTotal.setExistingStock(calculateTotal(patientDataReportViewModelUs.getExistingStock(), patientDataReportViewModelAPE.getExistingStock()));
        patientDataReportViewModelTotal.setCurrentTreatments(calculateTotal(patientDataReportViewModelUs.getCurrentTreatments(), patientDataReportViewModelAPE.getCurrentTreatments()));
        viewModels.add(patientDataReportViewModelUs);
        viewModels.add(patientDataReportViewModelAPE);
        viewModels.add(patientDataReportViewModelTotal);
    }

    private List<String> generateEmptyFields() {
        String[] treatments = new String[]{"0", "0", "0", "0"};
        return Arrays.asList(treatments);
    }

    private List<String> calculateTotal(List<String> valuesUs, List<String> valuesApe) {
        List<String> valuesTotal = new ArrayList<>();
        for (int index = 0; index < MALARIA_PRODUCTS_TOTAL; index++) {
            int totalExistingStock = Integer.valueOf(valuesUs.get(index)) + Integer.valueOf(valuesApe.get(index));
            valuesTotal.add(String.valueOf(totalExistingStock));
        }
        return valuesTotal;
    }

    @Override
    public void deleteDraft() {

    }

    @Override
    public boolean isDraft() {
        return false;
    }

    @Override
    protected void addSignature(String signature) {

    }
}
