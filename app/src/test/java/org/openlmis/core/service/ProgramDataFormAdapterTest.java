package org.openlmis.core.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import java.util.TimeZone;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataForm.Status;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.Signature;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ProgramDataColumnBuilder;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.network.adapter.ProgramDataFormAdapter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class ProgramDataFormAdapterTest {

  private ProgramDataFormAdapter programDataAdapter;
  private ProgramRepository mockProgramRepository;
  private Program program;

  @Before
  public void setUp() throws LMISException {
    mockProgramRepository = mock(ProgramRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    programDataAdapter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramDataFormAdapter.class);
    UserInfoMgr.getInstance().setUser(new User("user", "password"));
    program = new Program();
    program.setProgramCode(Constants.RAPID_TEST_PROGRAM_CODE);
    when(mockProgramRepository.queryByCode(Constants.RAPID_TEST_PROGRAM_CODE)).thenReturn(program);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    DateTimeZone.setDefault(DateTimeZone.UTC);
  }

  @Test
  public void shouldDeserializeProgramDataFormJson() throws LMISException {

    String json = JsonFileReader.readJson(getClass(), "SyncDownRapidTestsResponse.json");
    ProgramDataForm programDataForm = programDataAdapter
        .deserialize(new JsonParser().parse(json), null, null);
    assertThat(programDataForm.getProgram(), is(program));
    assertThat(programDataForm.getPeriodBegin(),
        is(DateUtil.parseString("2016-02-21", DateUtil.DB_DATE_FORMAT)));
    assertThat(programDataForm.getPeriodEnd(),
        is(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(programDataForm.getStatus(), is(Status.AUTHORIZED));
    assertThat(programDataForm.isSynced(), is(true));
    assertThat(programDataForm.getSubmittedTime(),
        is(DateUtil.parseString("2016-11-25 12:03:00", DateUtil.DATE_TIME_FORMAT)));
    assertThat(programDataForm.getProgramDataFormItemListWrapper().size(), is(8));
    assertThat(programDataForm.getProgramDataFormItemListWrapper().get(0).getForm(),
        is(programDataForm));
    assertThat(programDataForm.getProgramDataFormItemListWrapper().get(0).getName(),
        is("PUB_PHARMACY"));
    assertThat(
        programDataForm.getProgramDataFormItemListWrapper().get(0).getProgramDataColumn().getCode(),
        is("HIV-DETERMINE-CONSUME"));
    assertThat(programDataForm.getProgramDataFormItemListWrapper().get(0).getValue(), is(10));
    assertThat(programDataForm.getSignaturesWrapper().get(0).getSignature(), is("mystique"));
    assertThat(programDataForm.getSignaturesWrapper().get(0).getType(),
        is(Signature.TYPE.SUBMITTER));
    assertThat(programDataForm.getSignaturesWrapper().get(0).getForm(), is(programDataForm));
  }

  @Test
  public void shouldSerializeProgramDataFormToJson() throws LMISException {
    User user = new User();
    user.setFacilityId("123");
    UserInfoMgr.getInstance().setUser(user);
    ProgramDataForm programDataForm = new ProgramDataFormBuilder().setProgram(program)
        .setPeriod(DateUtil.parseString("2016-03-21", DateUtil.DB_DATE_FORMAT))
        .setStatus(Status.AUTHORIZED)
        .setSubmittedTime(DateUtil.parseString("2016-11-25 12:03:00", DateUtil.DATE_TIME_FORMAT))
        .setSignatures("mystique", Signature.TYPE.SUBMITTER)
        .setSignatures("magneto", Signature.TYPE.APPROVER)
        .setSynced(false).build();
    ProgramDataFormItem programDataFormItem1 = new ProgramDataFormItem("PUBLIC_PHARMACY",
        new ProgramDataColumnBuilder().setCode("HIV-DETERMINE-CONSUME").build(), 50);
    ProgramDataFormItem programDataFormItem2 = new ProgramDataFormItem("PUBLIC_PHARMACY",
        new ProgramDataColumnBuilder().setCode("HIV-DETERMINE-POSITIVE").build(), 20);
    ProgramDataFormItem programDataFormItem3 = new ProgramDataFormItem("WARD",
        new ProgramDataColumnBuilder().setCode("HIV-UNIGOLD-CONSUME").build(), 60);
    ProgramDataFormItem programDataFormItem4 = new ProgramDataFormItem("WARD",
        new ProgramDataColumnBuilder().setCode("HIV-UNIGOLD-POSITIVE").build(), 20);
    programDataForm.setProgramDataFormItemListWrapper(
        newArrayList(programDataFormItem1, programDataFormItem2, programDataFormItem3,
            programDataFormItem4));

    JsonElement jsonElement = programDataAdapter.serialize(programDataForm, null, null);

    assertThat(jsonElement.getAsJsonObject().get("facilityId").getAsInt(), is(123));
    assertThat(jsonElement.getAsJsonObject().get("programCode").getAsString(),
        is(Constants.RAPID_TEST_OLD_CODE));
    assertThat(jsonElement.getAsJsonObject().get("periodBegin").getAsString(), is("2016-03-21"));
    assertThat(jsonElement.getAsJsonObject().get("periodEnd").getAsString(), is("2016-04-20"));
    assertThat(jsonElement.getAsJsonObject().get("submittedTime").getAsString(),
        startsWith("20161125T120300.000"));
    assertThat(
        jsonElement.getAsJsonObject().get("programDataFormSignatures").getAsJsonArray().get(0)
            .getAsJsonObject().get("type").toString(), is("\"SUBMITTER\""));
    assertThat(
        jsonElement.getAsJsonObject().get("programDataFormSignatures").getAsJsonArray().get(0)
            .getAsJsonObject().get("name").toString(), is("\"mystique\""));
    assertThat(jsonElement.getAsJsonObject().get("programDataFormItems").getAsJsonArray().size(),
        is(4));
    assertThat(jsonElement.getAsJsonObject().get("programDataFormItems").getAsJsonArray().get(0)
        .getAsJsonObject().get("name").toString(), is("\"PUBLIC_PHARMACY\""));
    assertThat(jsonElement.getAsJsonObject().get("programDataFormItems").getAsJsonArray().get(0)
        .getAsJsonObject().get("columnCode").toString(), is("\"HIV-DETERMINE-CONSUME\""));
    assertThat(jsonElement.getAsJsonObject().get("programDataFormItems").getAsJsonArray().get(0)
        .getAsJsonObject().get("value").getAsInt(), is(50));
  }


  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProgramRepository.class).toInstance(mockProgramRepository);
    }
  }

}
