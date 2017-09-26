package org.openlmis.core.presenter;

import com.google.inject.Inject;

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
    public static final String US = "US";
    public static final String APE = "APE";
    public static final String TOTAL = "TOTAL";
    @Inject
    PatientDataService patientDataService;

    private List<PatientDataReportViewModel> viewModels;
    private List<Long> currentTreatmentsUs;
    private List<Long> currentTreatmentsApe;
    private List<Long> existingStockApe;
    private List<Long> existingStockUs;

    public PatientDataReportFormPresenter() {
        this.viewModels = new ArrayList<>();
        this.currentTreatmentsUs = generateEmptyFields();
        this.currentTreatmentsUs = generateEmptyFields();
        this.currentTreatmentsApe = generateEmptyFields();
        this.existingStockApe = generateEmptyFields();
    }

    public void setExistingStockUs(List<Long> existingStockUs) {
        this.existingStockUs = existingStockUs;
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public List<PatientDataReportViewModel> getViewModels(Period period) {
        generateViewModelsBySpecificPeriod(period);
        return viewModels;
    }

    public void generateViewModelsBySpecificPeriod(Period period) {
        viewModels.clear();
        PatientDataReportViewModel patientDataReportViewModelUs = generateViewModel(period, US, currentTreatmentsUs, patientDataService.getMalariaProductsStockHand());
        PatientDataReportViewModel patientDataReportViewModelAPE = generateViewModel(period, APE, currentTreatmentsApe, existingStockApe);
        List<Long> existingStockTotal = calculateTotal(patientDataReportViewModelUs.getExistingStock(), patientDataReportViewModelAPE.getExistingStock());
        List<Long> currentTreatmentsTotal = calculateTotal(patientDataReportViewModelUs.getCurrentTreatments(), patientDataReportViewModelAPE.getCurrentTreatments());
        PatientDataReportViewModel patientDataReportViewModelTotal = generateViewModel(period, TOTAL, currentTreatmentsTotal, existingStockTotal);
        viewModels.add(patientDataReportViewModelUs);
        viewModels.add(patientDataReportViewModelAPE);
        viewModels.add(patientDataReportViewModelTotal);
    }

    private PatientDataReportViewModel generateViewModel(Period period, String title, List<Long> currentTreatments, List<Long> existingStock) {
        PatientDataReportViewModel patientDataReportViewModel = new PatientDataReportViewModel(period);
        patientDataReportViewModel.setType(title);
        patientDataReportViewModel.setCurrentTreatments(currentTreatments);
        patientDataReportViewModel.setExistingStock(existingStock);
        return patientDataReportViewModel;
    }

    private List<Long> generateEmptyFields() {
        Long[] treatments = new Long[]{0L, 0L, 0L, 0L};
        return Arrays.asList(treatments);
    }

    public List<Long> calculateTotal(List<Long> valuesUs, List<Long> valuesApe) {
        List<Long> valuesTotal = new ArrayList<>();
        for (int index = 0; index < MALARIA_PRODUCTS_TOTAL; index++) {
            long totalExistingStock = valuesUs.get(index) + valuesApe.get(index);
            valuesTotal.add(totalExistingStock);
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

    public void setCurrentTreatmentsUs(List<Long> currentTreatmentsUs) {
        this.currentTreatmentsUs = currentTreatmentsUs;
    }

    public void setCurrentTreatmentsApe(List<Long> currentTreatmentsApe) {
        this.currentTreatmentsApe = currentTreatmentsApe;
    }

    public void setExistingStockApe(List<Long> existingStockApe) {
        this.existingStockApe = existingStockApe;
    }
//
//    public PatientDataReportViewModel calculateTotal(PatientDataReportViewModel patientDataReportViewModel, PatientDataReportViewModel patientDataReportViewModel1) {
//        PatientDataReportViewModel totalViewModel = getViewModels(p).get(2);
//        totalViewModel.setExistingStock(calculateTotal(patientDataReportViewModel.getExistingStock(), patientDataReportViewModel1.getExistingStock()));
//        totalViewModel.setCurrentTreatments(calculateTotal(patientDataReportViewModel.getCurrentTreatments(), patientDataReportViewModel1.getCurrentTreatments()));
//        return totalViewModel;
//    }
}
