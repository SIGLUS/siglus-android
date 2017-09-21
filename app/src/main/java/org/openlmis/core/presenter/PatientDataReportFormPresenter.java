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
    private List<String> currentTreatmentsUs;
    private List<String> existingStockUs;
    private List<String> currentTreatmentsApe;
    private List<String> existingStockApe;

    public PatientDataReportFormPresenter() {
        this.viewModels = new ArrayList<>();
        this.currentTreatmentsUs = generateEmptyFields();
        this.existingStockUs = generateEmptyFields();
        this.currentTreatmentsApe = generateEmptyFields();
        this.existingStockApe = generateEmptyFields();
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
        patientDataReportViewModelAPE.setUsApe("APE");
        patientDataReportViewModelTotal.setUsApe("TOTAL");
        generateExistingStock(patientDataReportViewModelUs, patientDataReportViewModelAPE);
        generateCurrentTreatments(patientDataReportViewModelUs, patientDataReportViewModelAPE);
        patientDataReportViewModelTotal.setExistingStock(calculateTotal(patientDataReportViewModelUs.getExistingStock(), patientDataReportViewModelAPE.getExistingStock()));
        patientDataReportViewModelTotal.setCurrentTreatments(calculateTotal(patientDataReportViewModelUs.getCurrentTreatments(), patientDataReportViewModelAPE.getCurrentTreatments()));
        viewModels.add(patientDataReportViewModelUs);
        viewModels.add(patientDataReportViewModelAPE);
        viewModels.add(patientDataReportViewModelTotal);
    }



    private void generateExistingStock(PatientDataReportViewModel patientDataReportViewModelUs, PatientDataReportViewModel patientDataReportViewModelAPE) {
//        if (existingStockUs.isEmpty()) {
//            patientDataReportViewModelUs.setExistingStock(generateEmptyFields());
//        } else {
//            patientDataReportViewModelUs.setExistingStock(existingStockUs);
//        }
//        if (existingStockApe.isEmpty()) {
//            patientDataReportViewModelAPE.setExistingStock(generateEmptyFields());
//        } else {
//            patientDataReportViewModelAPE.setExistingStock(existingStockApe);
//        }
        patientDataReportViewModelUs.setExistingStock(patientDataService.getMalariaProductsStockHand());
        patientDataReportViewModelAPE.setExistingStock(existingStockApe);
    }

    private void generateCurrentTreatments(PatientDataReportViewModel patientDataReportViewModelUs, PatientDataReportViewModel patientDataReportViewModelAPE) {
//        if (currentTreatmentsUs.isEmpty()) {
//            patientDataReportViewModelUs.setExistingStock(generateEmptyFields());
//        } else {
//            patientDataReportViewModelUs.setExistingStock(currentTreatmentsUs);
//        }
//        if (currentTreatmentsApe.isEmpty()) {
//            patientDataReportViewModelAPE.setExistingStock(generateEmptyFields());
//        } else {
//            patientDataReportViewModelAPE.setExistingStock(currentTreatmentsApe);
//        }
        patientDataReportViewModelUs.setCurrentTreatments(currentTreatmentsUs);
        patientDataReportViewModelAPE.setCurrentTreatments(currentTreatmentsApe);
    }

    private List<String> generateEmptyFields() {
        String[] treatments = new String[]{"0", "0", "0", "0"};
        return Arrays.asList(treatments);
    }

    public List<String> calculateTotal(List<String> valuesUs, List<String> valuesApe) {
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

    public void setCurrentTreatmentsUs(List<String> currentTreatmentsUs) {
        this.currentTreatmentsUs = currentTreatmentsUs;
    }

    public void setExistingStockUs(List<String> existingStockUs) {
        this.existingStockUs = existingStockUs;
    }

    public void setCurrentTreatmentsApe(List<String> currentTreatmentsApe) {
        this.currentTreatmentsApe = currentTreatmentsApe;
    }

    public void setExistingStockApe(List<String> existingStockApe) {
        this.existingStockApe = existingStockApe;
    }

    public PatientDataReportViewModel calculateTotal(PatientDataReportViewModel patientDataReportViewModel, PatientDataReportViewModel patientDataReportViewModel1) {
        PatientDataReportViewModel totalViewModel = getViewModels().get(2);
        totalViewModel.setExistingStock(calculateTotal(patientDataReportViewModel.getExistingStock(), patientDataReportViewModel1.getExistingStock()));
        totalViewModel.setCurrentTreatments(calculateTotal(patientDataReportViewModel.getCurrentTreatments(), patientDataReportViewModel1.getCurrentTreatments()));
        return totalViewModel;
    }
}
