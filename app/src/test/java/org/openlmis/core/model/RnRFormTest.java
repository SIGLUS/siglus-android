package org.openlmis.core.model;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

@RunWith(LMISTestRunner.class)
public class RnRFormTest {

  private RnRForm rnRForm;

  @Before
  public void setUp() throws Exception {
    rnRForm = new RnRForm();
  }

  @Test
  public void shouldReturnListWithDeactivatedItemsWhenDeactivateProgramToggleOn() {
    Product activeSupportedProduct = new ProductBuilder().setCode("P1").setIsActive(true).build();
    Product inactiveProduct = new ProductBuilder().setCode("P2").setIsActive(false).build();
    Product unsupportedProduct = new ProductBuilder().setCode("P3").setIsActive(true).build();

    RnrFormItem activeRnrProduct = new RnrFormItemBuilder().setProduct(activeSupportedProduct)
        .build();
    RnrFormItem inactiveRnrProduct = new RnrFormItemBuilder().setProduct(inactiveProduct).build();
    RnrFormItem unsupportedRnrProduct = new RnrFormItemBuilder().setProduct(unsupportedProduct)
        .build();

    rnRForm.setRnrFormItemListWrapper(
        newArrayList(activeRnrProduct, inactiveRnrProduct, unsupportedRnrProduct));

    List<RnrFormItem> rnrFormDeactivatedItemList = rnRForm
        .getDeactivatedAndUnsupportedProductItems(Arrays.asList("P1", "P2"));
    assertEquals(2, rnrFormDeactivatedItemList.size());
    assertEquals(false, rnrFormDeactivatedItemList.get(0).getProduct().isActive());
  }

  @Test
  public void shouldGetNonKitFormItemAndKitFormItem() throws Exception {
    Product kitProduct = new ProductBuilder().setIsActive(true).setIsKit(true).setCode("kit")
        .build();
    Product product = new ProductBuilder().setIsActive(true).setIsKit(false).setCode("product")
        .build();
    RnrFormItem kitRnrProduct = new RnrFormItemBuilder().setProduct(kitProduct).build();
    RnrFormItem rnrProduct = new RnrFormItemBuilder().setProduct(product).build();

    rnRForm.setRnrFormItemListWrapper(newArrayList(rnrProduct, kitRnrProduct));

    List<RnrFormItem> rnrNonKitItems = rnRForm.getRnrItems(IsKit.No);
    assertEquals(1, rnrNonKitItems.size());
    assertFalse(rnrNonKitItems.get(0).getProduct().isKit());

    List<RnrFormItem> rnrKitItems = rnRForm.getRnrItems(IsKit.Yes);
    assertEquals(1, rnrKitItems.size());
    assertTrue(rnrKitItems.get(0).getProduct().isKit());

  }

  @Test
  public void shouldGenerateRnRFromByLastPeriod() throws Exception {
    Date generateDate = DateUtil.parseString("10/06/2015", DateUtil.SIMPLE_DATE_FORMAT);
    RnRForm rnRForm = RnRForm.init(new Program(), generateDate);

    assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT),
        is("21/05/2015"));
    assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT),
        is("20/06/2015"));

    generateDate = DateUtil.parseString("30/05/2015", DateUtil.SIMPLE_DATE_FORMAT);
    rnRForm = RnRForm.init(new Program(), generateDate);

    assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT),
        is("21/05/2015"));
    assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT),
        is("20/06/2015"));

    generateDate = DateUtil.parseString("25/01/2015", DateUtil.SIMPLE_DATE_FORMAT);
    rnRForm = RnRForm.init(new Program(), generateDate);

    assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT),
        is("21/12/2014"));
    assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT),
        is("20/01/2015"));
  }


  @Test
  public void shouldGenerateRnRFromByCurrentPeriod() throws Exception {
    Date generateDate = DateUtil.parseString("30/06/2015", DateUtil.SIMPLE_DATE_FORMAT);
    RnRForm rnRForm = RnRForm.init(new Program(), generateDate);

    assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT),
        is("21/06/2015"));
    assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT),
        is("20/07/2015"));

    generateDate = DateUtil.parseString("05/07/2015", DateUtil.SIMPLE_DATE_FORMAT);
    rnRForm = RnRForm.init(new Program(), generateDate);

    assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT),
        is("21/06/2015"));
    assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT),
        is("20/07/2015"));

    generateDate = DateUtil.parseString("28/12/2015", DateUtil.SIMPLE_DATE_FORMAT);
    rnRForm = RnRForm.init(new Program(), generateDate);

    assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT),
        is("21/12/2015"));
    assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT),
        is("20/01/2016"));
  }

  @Test
  public void shouldInitFormMissedStatusWhenHasMissed() throws Exception {
    Program program = new Program();
    program.setId(123);
    program.setProgramCode(Constants.MMIA_PROGRAM_CODE);

    DateTime periodBegin = new DateTime(
        DateUtil.parseString("2015-06-21 10:10:10", DateUtil.DATE_TIME_FORMAT));
    DateTime periodEnd = new DateTime(
        DateUtil.parseString("2015-07-21 11:11:11", DateUtil.DATE_TIME_FORMAT));

    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-07-26 10:10:10", DateUtil.DATE_TIME_FORMAT).getTime());
    RnRForm form = RnRForm.init(program, new Period(periodBegin, periodEnd), false);
    assertTrue(form.isMissed());

    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-07-25 10:10:10", DateUtil.DATE_TIME_FORMAT).getTime());
    form = RnRForm.init(program, new Period(periodBegin, periodEnd), false);
    assertFalse(form.isMissed());

    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-06-25 10:10:10", DateUtil.DATE_TIME_FORMAT).getTime());
    form = RnRForm.init(program, new Period(periodBegin, periodEnd), false);
    assertFalse(form.isMissed());
  }

  @Test
  public void shouldInitEmergencyForm() throws Exception {
    Program program = new Program();
    program.setId(123);
    program.setProgramCode(Constants.MMIA_PROGRAM_CODE);

    DateTime periodBegin = new DateTime(
        DateUtil.parseString("2015-06-21 10:10:10", DateUtil.DATE_TIME_FORMAT));
    DateTime periodEnd = new DateTime(
        DateUtil.parseString("2015-07-21 11:11:11", DateUtil.DATE_TIME_FORMAT));

    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-07-26 10:10:10", DateUtil.DATE_TIME_FORMAT).getTime());
    RnRForm form = RnRForm.init(program, new Period(periodBegin, periodEnd), true);
    assertFalse(form.isMissed());
    assertTrue(form.isEmergency());
  }

  @Test
  public void shouldSortRnrFormItemByProductCode() throws Exception {
    List<RnrFormItem> rnrFormItems = new ArrayList<>();
    rnrFormItems.add(generateRnrFormItem("03A02", false));
    rnrFormItems.add(generateRnrFormItem("01A02", false));
    rnrFormItems.add(generateRnrFormItem("10B02", false));
    rnRForm.setStatus(Status.AUTHORIZED);
    rnRForm.setRnrFormItemListWrapper(rnrFormItems);

    List<RnrFormItem> sortedRnrFormItems = rnRForm.getRnrFormItemListWrapper();
    assertEquals("01A02", sortedRnrFormItems.get(0).getProduct().getCode());
    assertEquals("03A02", sortedRnrFormItems.get(1).getProduct().getCode());
    assertEquals("10B02", sortedRnrFormItems.get(2).getProduct().getCode());
  }

  @Test
  public void shouldSortRnrByCategoryFirst() throws Exception {
    List<RnrFormItem> rnrFormItems = new ArrayList<>();
    rnrFormItems.add(generateRnrFormItem("A3", false));
    rnrFormItems.add(generateRnrFormItem("B3", true));
    rnrFormItems.add(generateRnrFormItem("B1", false));
    rnrFormItems.add(generateRnrFormItem("A2", true));
    rnrFormItems.add(generateRnrFormItem("B2", false));
    rnrFormItems.add(generateRnrFormItem("A1", false));
    rnRForm.setStatus(Status.DRAFT);
    rnRForm.setRnrFormItemListWrapper(rnrFormItems);

    List<RnrFormItem> sortedRnrFormItems = rnRForm.getRnrFormItemListWrapper();
    assertEquals("A1", sortedRnrFormItems.get(0).getProduct().getCode());
    assertEquals("A3", sortedRnrFormItems.get(1).getProduct().getCode());
    assertEquals("B1", sortedRnrFormItems.get(2).getProduct().getCode());
    assertEquals("B2", sortedRnrFormItems.get(3).getProduct().getCode());
    assertEquals("A2", sortedRnrFormItems.get(4).getProduct().getCode());
    assertEquals("B3", sortedRnrFormItems.get(5).getProduct().getCode());
  }

  private RnrFormItem generateRnrFormItem(String productCode, boolean manualAdd) {
    return new RnrFormItemBuilder().setProduct(new ProductBuilder().setCode(productCode).build())
        .setManualAdd(manualAdd).build();
  }

  @Test
  public void shouldAddSignature() throws Exception {
    rnRForm.setStatus(Status.DRAFT);
    rnRForm.addSignature("signature1");
    assertEquals(Status.SUBMITTED, rnRForm.getStatus());
    assertEquals(rnRForm.getSignaturesWrapper().get(0).getSignature(), "signature1");

    rnRForm.addSignature("signature2");
    assertEquals(Status.AUTHORIZED, rnRForm.getStatus());
    assertEquals(rnRForm.getSignaturesWrapper().get(1).getSignature(), "signature2");

    rnRForm = new RnRForm();
    rnRForm.setStatus(Status.DRAFT_MISSED);
    rnRForm.addSignature("signature1");
    assertEquals(Status.SUBMITTED_MISSED, rnRForm.getStatus());
    assertEquals(rnRForm.getSignaturesWrapper().get(0).getSignature(), "signature1");

    rnRForm.addSignature("signature2");
    assertEquals(Status.AUTHORIZED, rnRForm.getStatus());
    assertEquals(rnRForm.getSignaturesWrapper().get(1).getSignature(), "signature2");
  }
}