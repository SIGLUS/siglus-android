package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.PatientDataReport;
import org.openlmis.core.model.Period;
import org.openlmis.core.service.PatientDataService;
import org.openlmis.core.utils.ToastUtil;
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
        this.currentTreatmentsApe = generateEmptyFields();
        this.existingStockUs = generateEmptyFields();
        this.existingStockApe = generateEmptyFields();
    }

    public void setExistingStockUs(List<Long> existingStockUs) {
        this.existingStockUs = existingStockUs;
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public List<PatientDataReportViewModel> getViewModels(Period period, boolean isUpdate) {
        generateViewModelsBySpecificPeriod(period, isUpdate);
        return viewModels;
    }

    public void generateViewModelsBySpecificPeriod(Period period, Boolean isUpdate) {
        PatientDataReportViewModel patientDataReportViewModelUs;
        PatientDataReportViewModel patientDataReportViewModelAPE;
        if (!isUpdate) {
            patientDataReportViewModelUs = generateViewModel(period, US, currentTreatmentsUs, patientDataService.getMalariaProductsStockHand(), Boolean.FALSE);
            patientDataReportViewModelAPE = generateViewModel(period, APE, currentTreatmentsApe, existingStockApe, Boolean.FALSE);
        } else {
            patientDataReportViewModelUs = generateViewModel(period, US, currentTreatmentsUs, existingStockUs, Boolean.TRUE);
            patientDataReportViewModelAPE = generateViewModel(period, APE, currentTreatmentsApe, existingStockApe, Boolean.TRUE);
        }
        List<Long> existingStockTotal = calculateTotal(patientDataReportViewModelUs.getExistingStock(), patientDataReportViewModelAPE.getExistingStock());
        List<Long> currentTreatmentsTotal = calculateTotal(patientDataReportViewModelUs.getCurrentTreatments(), patientDataReportViewModelAPE.getCurrentTreatments());
        PatientDataReportViewModel patientDataReportViewModelTotal = generateViewModel(period, TOTAL, currentTreatmentsTotal, existingStockTotal, Boolean.FALSE);
        viewModels.clear();
        viewModels.add(patientDataReportViewModelUs);
        viewModels.add(patientDataReportViewModelAPE);
        viewModels.add(patientDataReportViewModelTotal);
    }

    private PatientDataReportViewModel generateViewModel(Period period, String title, List<Long> currentTreatments, List<Long> existingStock, Boolean isUpdate) {
        PatientDataReportViewModel patientDataReportViewModel = new PatientDataReportViewModel(period);
        patientDataReportViewModel.setType(title);
        if(!isUpdate){
            try {
                if (title.equals(US))  {
                    PatientDataReport patientDataReport = patientDataService.getExistingByModelPerPeriod(period.getBegin(), period.getEnd(), title);
                    if (patientDataReport != null) {
                        currentTreatments = patientDataReport.getCurrentTreatments();
                        existingStock = patientDataReport.getExistingStocks();
                    }
                    this.currentTreatmentsUs = currentTreatments;
                    this.existingStockUs = existingStock;
                }

                if (title.equals(APE)){
                    PatientDataReport patientDataReport = patientDataService.getExistingByModelPerPeriod(period.getBegin(), period.getEnd(), title);
                    if (patientDataReport != null) {
                        currentTreatments = patientDataReport.getCurrentTreatments();
                        existingStock = patientDataReport.getExistingStocks();
                    }
                    this.currentTreatmentsApe = currentTreatments;
                    this.existingStockApe = existingStock;
                }
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
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


    public void saveForm() {
        try {
            Boolean isSuccess = patientDataService.savePatientDataMovementsPerPeriod(viewModels);
            if (isSuccess) {
                ToastUtil.show("Successfully Saved");
            }
        } catch (Exception e) {
            new LMISException(e).reportToFabric();
        }
    }
}
