/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */
package org.openlmis.core.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.VIA_PROGRAM_CODE;
import static org.openlmis.core.utils.DateUtil.DATE_TIME_FORMAT;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.RnrFormSignatureRepository;
import org.openlmis.core.network.adapter.RnrFormAdapter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RnrFormAdapterTest {

  private RnrFormAdapter rnrFormAdapter;
  private RnRForm rnRForm;
  private ProductRepository mockProductRepository;
  private ProgramRepository mockProgramRepository;
  private RnrFormRepository mockRnrFormRepository;
  private RnrFormSignatureRepository mockRnrFormSignatureRepository;

  @Before
  public void setUp() throws LMISException {
    mockProductRepository = mock(ProductRepository.class);
    mockProgramRepository = mock(ProgramRepository.class);
    mockRnrFormRepository = mock(RnrFormRepository.class);
    mockRnrFormSignatureRepository = mock(RnrFormSignatureRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    rnrFormAdapter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RnrFormAdapter.class);
    rnRForm = new RnRForm();
    rnRForm.setPeriodBegin(DateUtil.parseString("2020-05-15 01:01:11", DATE_TIME_FORMAT));
    rnRForm.setPeriodEnd(DateUtil.parseString("2020-06-15 01:01:11", DATE_TIME_FORMAT));
    UserInfoMgr.getInstance().setUser(new User("user", "password"));
    Program program = new Program();
    program.setProgramCode(MMIA_PROGRAM_CODE);
    rnRForm.setProgram(program);
  }

  @Test
  public void shouldSerializeRnrFormWithCommentsToJsonObject() throws LMISException {
    rnRForm.setComments("XYZ");
    rnRForm.setSubmittedTime(DateUtil.today());
    rnRForm.setEmergency(true);

    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);
    assertEquals("\"XYZ\"", rnrJson.getAsJsonObject().get("comments").toString());
    assertEquals("true", rnrJson.getAsJsonObject().get("emergency").toString());
  }

  @Test
  public void shouldSerializeRnrFormWithSubmittedTime() throws Exception {
    // given
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/GMT-8"));
    rnRForm.setSubmittedTime(dateFormat.parse("2010-01-31T14:32:19Z"));

    // when
    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);

    // then
    assertEquals("\"2010-01-31T06:32:19.000Z\"",
        rnrJson.getAsJsonObject().get("clientSubmittedTime").toString());
  }

  @Test
  public void shouldSerializeRnrFormWithExpirationDate() throws Exception {

    ArrayList<RnrFormItem> rnrFormItemListWrapper = new ArrayList<>();
    RnrFormItem rnrFormItem = new RnrFormItem();
    Product product = new Product();
    product.setCode("P1");
    rnrFormItem.setProduct(product);
    rnrFormItem.setValidate("10/11/2015");
    rnrFormItemListWrapper.add(rnrFormItem);
    rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

    rnRForm.setSubmittedTime(DateUtil.parseString("2015-10-14 01:01:11", "yyyy-MM-dd HH:mm:ss"));

    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);
    assertThat(rnrJson.getAsJsonObject().get("products").getAsJsonArray().get(0).getAsJsonObject()
        .get("expirationDate").toString(), is("\"10/11/2015\""));
  }


  @Test
  public void shouldSerializeRnrFormWithRnrFormItem() throws Exception {
    // given
    ArrayList<RnrFormItem> rnrFormItemListWrapper = new ArrayList<>();
    RnrFormItem object = new RnrFormItem();
    Product product = new Product();
    product.setCode("P1");
    object.setProduct(product);
    object.setInventory(100L);
    object.setValidate("10/11/2015");
    object.setRequestAmount(111L);
    object.setApprovedAmount(222L);
    rnrFormItemListWrapper.add(object);
    rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

    // when
    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);

    // then
    JsonObject productInfo = rnrJson.getAsJsonObject().get("products").getAsJsonArray().get(0)
        .getAsJsonObject();
    assertThat(productInfo.get("productCode").toString(), is("\"P1\""));
    assertThat(productInfo.get("stockOnHand").toString(), is("100"));
    assertThat(productInfo.get("expirationDate").toString(), is("\"10/11/2015\""));
    assertThat(productInfo.get("requestedQuantity").toString(), is("111"));
    assertThat(productInfo.get("authorizedQuantity").toString(), is("222"));

  }

  @Test
  public void shouldSerializeRnrFormWithRegimen() throws Exception {
    ArrayList<RegimenItem> regimenItems = new ArrayList<>();
    RegimenItem item = new RegimenItem();
    Regimen regimen = new Regimen();
    regimen.setName("name1");
    regimen.setCode("code1");
    item.setRegimen(regimen);
    item.setAmount(10L);
    regimenItems.add(item);
    rnRForm.setRegimenItemListWrapper(regimenItems);

    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);

    JsonObject regimens = rnrJson.getAsJsonObject().get("regimens").getAsJsonArray().get(0)
        .getAsJsonObject();
    assertThat(regimens.get("code").toString(), is("\"code1\""));
    assertThat(regimens.get("name").toString(), is("\"name1\""));
    assertThat(regimens.get("patientsOnTreatment").toString(), is("10"));
  }

  @Test
  public void shouldSerializeRnrFormWithConsultationNumber() throws Exception {
    // given
    ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();
    BaseInfoItem baseInfoItem = new BaseInfoItem();
    baseInfoItem.setValue("15");
    baseInfoItem.setName("Name1");
    baseInfoItems.add(baseInfoItem);
    rnRForm.setBaseInfoItemListWrapper(baseInfoItems);
    rnRForm.setProgram(generateProgram(VIA_PROGRAM_CODE, VIA_PROGRAM_CODE));

    // when
    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);

    // then
    Long consultationNumber = rnrJson.getAsJsonObject().get("consultationNumber").getAsLong();
    assertEquals(Long.valueOf(15), consultationNumber);
  }

  @Test
  public void shouldSerializeRnrFormSignatureWithBaseInfo() throws Exception {
    // given
    List<RnRFormSignature> rnRFormSignatureList = new ArrayList<>();
    rnRFormSignatureList.add(new RnRFormSignature(rnRForm, "abc", RnRFormSignature.TYPE.SUBMITTER));
    when(mockRnrFormSignatureRepository.queryByRnrFormId(any(Long.class)))
        .thenReturn(rnRFormSignatureList);

    // when
    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);

    // then
    JsonObject rnrSignature = rnrJson.getAsJsonObject().get("signatures").getAsJsonArray().get(0)
        .getAsJsonObject();
    assertThat(rnrSignature.get("name").toString(), is("\"abc\""));
    assertThat(rnrSignature.get("type").toString(),
        is("\"" + RnRFormSignature.TYPE.SUBMITTER.toString() + "\""));
  }

  @Test
  public void shouldDeserializeRnrFormJson() throws LMISException {
    // given
    when(mockProductRepository.getByCode(anyString())).thenReturn(new Product());
    Program viaProgram = new Program();
    viaProgram.setProgramCode(Program.VIA_CODE);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(viaProgram);

    // when
    String json = JsonFileReader.readJson(getClass(), "RequisitionResponse.json");
    RnRForm rnRForm = rnrFormAdapter.deserialize(new JsonParser().parse(json), null, null);

    //then
    RnrFormItem rnrFormItem = rnRForm.getRnrFormItemListWrapper().get(0);
    assertEquals(Long.valueOf(10), rnrFormItem.getInitialAmount());
    assertEquals(Long.valueOf(10), rnrFormItem.getInventory());
    assertEquals(Long.valueOf(20), rnrFormItem.getRequestAmount());
    assertEquals(Long.valueOf(30), rnrFormItem.getApprovedAmount());
    verify(mockProductRepository).getByCode("08S17");

    RegimenItem regimenItem = rnRForm.getRegimenItemListWrapper().get(0);
    assertEquals(Long.valueOf(1), regimenItem.getAmount());

    BaseInfoItem baseInfoItem = rnRForm.getBaseInfoItemListWrapper().get(0);
    assertEquals("consultation", baseInfoItem.getName());
    assertEquals("30", baseInfoItem.getValue());
  }

  @Test
  public void shouldDeserializeMMIARnrFormJsonForProduct() throws LMISException {
    // given
    when(mockProductRepository.getByCode(anyString())).thenReturn(new Product());
    Program MMIAProgram = new Program();
    MMIAProgram.setProgramCode(Program.TARV_CODE);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(MMIAProgram);

    // when
    RnRForm rnRForm = deserializeMMIAResponse();

    //then
    assertEquals("xing", rnRForm.getComments());
    RnrFormItem rnrFormItem = rnRForm.getRnrFormItemListWrapper().get(0);
    assertEquals(Long.valueOf(3), rnrFormItem.getInitialAmount());
    assertEquals(Long.valueOf(0), rnrFormItem.getInventory());
    assertEquals(6, rnrFormItem.getReceived());
    assertEquals(Long.valueOf(2), rnrFormItem.getIssued());
    verify(mockProductRepository).getByCode("08S01ZZ");
  }

  @Test
  public void shouldDeserializeMMIARnrFormJsonForRegimen() throws LMISException {
    // given
    when(mockProductRepository.getByCode(anyString())).thenReturn(new Product());
    Program MMIAProgram = new Program();
    MMIAProgram.setProgramCode(Program.TARV_CODE);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(MMIAProgram);

    // when
    RnRForm rnRForm = deserializeMMIAResponse();

    //then
    RegimenItem regimenItem = rnRForm.getRegimenItemListWrapper().get(0);
    assertEquals(Long.valueOf(13), regimenItem.getAmount());
    assertEquals(Long.valueOf(14), regimenItem.getPharmacy());
  }

  @Test
  public void shouldDeserializeMMIARnrFormJsonForRegimenSummaryLineItems() throws LMISException {
    // given
    when(mockProductRepository.getByCode(anyString())).thenReturn(new Product());
    Program MMIAProgram = new Program();
    MMIAProgram.setProgramCode(Program.TARV_CODE);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(MMIAProgram);

    // when
    RnRForm rnRForm = deserializeMMIAResponse();

    //then
    RegimenItemThreeLines regimenItem = rnRForm.getRegimenThreeLineListWrapper().get(0);
    assertEquals(Long.valueOf(33), regimenItem.getPatientsAmount());
    assertEquals(Long.valueOf(44), regimenItem.getPharmacyAmount());
  }

  @Test
  public void shouldDeserializeMMIARnrFormJsonForPatientLineItems() throws LMISException {
    // given
    when(mockProductRepository.getByCode(anyString())).thenReturn(new Product());
    Program MMIAProgram = new Program();
    MMIAProgram.setProgramCode(Program.TARV_CODE);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(MMIAProgram);

    // when
    RnRForm rnRForm = deserializeMMIAResponse();

    //then
    List<BaseInfoItem> baseInfoItems = rnRForm.getBaseInfoItemListWrapper();
    assertEquals(23, baseInfoItems.size());
    BaseInfoItem baseInfoItem = baseInfoItems.get(0);
    assertEquals("table_patients_adults_key", baseInfoItem.getName());
    assertEquals("table_patients_key", baseInfoItem.getTableName());
    assertEquals("6", baseInfoItem.getValue());
  }


  @Test
  public void shouldSerializeRnrFormWithPeriodDate() throws Exception {
    // given
    UserInfoMgr.getInstance().setUser(new User("user", "password"));

    rnRForm.setPeriodBegin(DateUtil.parseString("2015-10-15 01:01:11", "yyyy-MM-dd HH:mm:ss"));
    rnRForm.setPeriodEnd(DateUtil.parseString("2015-11-15 01:01:11", "yyyy-MM-dd HH:mm:ss"));

    // when
    JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);

    // then
    assertNotNull(rnrJson.getAsJsonObject().get("actualStartDate"));
    assertNotNull(rnrJson.getAsJsonObject().get("actualEndDate"));
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProductRepository.class).toInstance(mockProductRepository);
      bind(ProgramRepository.class).toInstance(mockProgramRepository);
      bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
      bind(RnrFormSignatureRepository.class).toInstance(mockRnrFormSignatureRepository);
    }
  }

  private Program generateProgram(String programCode, String programName) {
    return Program.builder()
        .programCode(programCode)
        .programName(programName)
        .isSupportEmergency(false)
        .build();
  }

  private RnRForm deserializeMMIAResponse() {
    String json = JsonFileReader.readJson(getClass(), "RequisitionMMIAResponse.json");
    return rnrFormAdapter.deserialize(new JsonParser().parse(json), null, null);
  }

}
