package org.openlmis.core.view.activity;

import android.os.Bundle;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.repository.PatientDispensationRepository;

import java.util.Date;
import java.util.Random;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_ptv_report_form)
public class PTVDataReportFormActivity extends BaseActivity {

    @Inject
    PatientDispensationRepository patientDispensationRepository;

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PTVProgram ptvProgram = new PTVProgram();
        Date today = DateTime.now().toDate();
        Date startPeriod = new Date();
        Date endPeriod = new Date();
        ptvProgram.setStartPeriod(startPeriod);
        ptvProgram.setEndPeriod(endPeriod);
        ptvProgram.setCreatedBy("TWUIO");
        ptvProgram.setVerifiedBy("MZ");
        int typeRandomPosition = new Random().nextInt(2);
        int totalPatients = new Random().nextInt();
        PatientDispensation patientDispensation = new PatientDispensation();
        PatientDispensation.PatientDispensationType patientDispensationType = PatientDispensation.PatientDispensationType.values()[typeRandomPosition];
        patientDispensation.setType(patientDispensationType);
        patientDispensation.setTotal(totalPatients);
        patientDispensation.setPtvProgram(ptvProgram);


        try {
            patientDispensationRepository.save(patientDispensation);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }
}
