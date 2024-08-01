package org.openlmis.core.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.builder.RnRFormBuilder;
import org.openlmis.core.utils.Constants;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RnrFormSignatureRepositoryTest {

  private RnrFormRepository rnrFormRepository;
  private RnrFormSignatureRepository signatureRepository;

  @Before
  public void setUp() throws Exception {
    rnrFormRepository = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(RnrFormRepository.class);
    signatureRepository = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(RnrFormSignatureRepository.class);
  }

  @Test
  public void shouldGetSignatureByRnrForm() throws LMISException {
    Program program = new Program();
    program.setProgramCode(Constants.VIA_PROGRAM_CODE);

    RnRForm form = new RnRFormBuilder().setComments("Submitted Form")
        .setStatus(Status.SUBMITTED)
        .setProgram(program).build();
    form.getSignaturesWrapper()
        .add(new RnRFormSignature(form, "Submitter Signature", RnRFormSignature.TYPE.SUBMITTER));
    form.getSignaturesWrapper()
        .add(new RnRFormSignature(form, "Approver Signature", RnRFormSignature.TYPE.APPROVER));
    rnrFormRepository.createAndRefresh(form);
    rnrFormRepository.createOrUpdateWithItems(form);

    List<RnRFormSignature> signatures = signatureRepository.queryByRnrFormId(form.getId());

    assertThat(signatures.size(), is(2));
    assertThat(signatures.get(0).getSignature(), is("Submitter Signature"));
    assertThat(signatures.get(1).getSignature(), is("Approver Signature"));

    signatureRepository.batchDelete(signatures);
    List<RnRFormSignature> deleted = signatureRepository.queryByRnrFormId(form.getId());
    assertThat(deleted.size(), is(0));
  }

}