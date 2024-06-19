package org.openlmis.core.view.viewmodel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.core.enumeration.MMITGridErrorType.EMPTY_POSITIVE;
import static org.openlmis.core.enumeration.MMITGridErrorType.POSITIVE_MORE_THAN_CONSUMPTION;
import static org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.RapidTestGridColumnCode.POSITIVE_HIV;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enumeration.MMITGridErrorType;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.view.viewmodel.RapidTestFormGridViewModel.ColumnCode;

@RunWith(LMISTestRunner.class)
public class RapidTestFormGridViewModelTest {

  public static final String ACC_EMERGENCY = "ACC_EMERGENCY";
  public static final String VALUE_100 = "100";

  RapidTestFormGridViewModel viewModel = new RapidTestFormGridViewModel(
      RapidTestFormGridViewModel.ColumnCode.MALARIA);

  RapidTestFormGridViewModel duoTestViewModel;
  private static final String VALUE_50 = "50";

  @Before
  public void setUp() throws Exception {
    duoTestViewModel = new RapidTestFormGridViewModel(
        ColumnCode.DUOTESTEHIVSIFILIS);
  }

  @Test
  public void shouldValidate() {
    assertTrue(viewModel.validate());

    viewModel.setConsumptionValue(VALUE_100);
    assertFalse(viewModel.validate());

    viewModel.setPositiveValue(VALUE_100);
    viewModel.setConsumptionValue("");
    assertFalse(viewModel.validate());

    viewModel.setConsumptionValue("99");
    assertFalse(viewModel.validate());

    viewModel.setConsumptionValue(VALUE_100);
    assertFalse(viewModel.validate());

    viewModel.setUnjustifiedValue(VALUE_100);
    assertTrue(viewModel.validate());
  }

  @Test
  public void shouldReturnTrueWhenValidateIsCalledByDuoTestAndAllEmpty() {
    assertTrue(duoTestViewModel.validate());
  }

  @Test
  public void shouldReturnFalseWhenValidateIsCalledByDuoTestAndConsumptionLessThanPositive() {
    // given
    duoTestViewModel.setPositiveHivValue(VALUE_100);
    duoTestViewModel.setPositiveSyphilisValue(VALUE_100);
    duoTestViewModel.setConsumptionValue(VALUE_100);
    duoTestViewModel.setUnjustifiedValue(VALUE_100);
    // when
    boolean actualResult = duoTestViewModel.validate();
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldReturnTrueWhenValidateIsCalledByDuoTestAndConsumptionEqualToPositive() {
    // given
    duoTestViewModel.setPositiveHivValue(VALUE_50);
    duoTestViewModel.setPositiveSyphilisValue(VALUE_50);
    duoTestViewModel.setConsumptionValue(VALUE_100);
    duoTestViewModel.setUnjustifiedValue(VALUE_100);
    // when
    boolean actualResult = duoTestViewModel.validate();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void shouldReturnTrueWhenValidateIsCalledByDuoTestAndConsumptionGreaterThanPositive() {
    // given
    duoTestViewModel.setPositiveHivValue(VALUE_50);
    duoTestViewModel.setPositiveSyphilisValue("");
    duoTestViewModel.setConsumptionValue(VALUE_100);
    duoTestViewModel.setUnjustifiedValue(VALUE_100);
    // when
    boolean actualResult = duoTestViewModel.validate();
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldConvertFormGridViewModelToDataModel() {
    viewModel.setConsumptionValue("20");
    viewModel.setPositiveValue("1001");
    viewModel.setUnjustifiedValue("121");
    UsageColumnsMap consumeColumn = new UsageColumnsMap();
    consumeColumn.setCode("CONSUME_MALARIA");
    viewModel.setConsumeColumn(consumeColumn);
    UsageColumnsMap positiveColumn = new UsageColumnsMap();
    positiveColumn.setCode("POSITIVE_MALARIA");
    viewModel.setPositiveColumn(positiveColumn);
    UsageColumnsMap unjustifiedColumn = new UsageColumnsMap();
    unjustifiedColumn.setCode("UNJUSTIFIED_MALARIA");

    MovementReasonManager.MovementReason reason = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, ACC_EMERGENCY, "Acc emergency");
    List<TestConsumptionItem> programDataFormItems = viewModel
        .convertFormGridViewModelToDataModel(reason);
    assertThat(programDataFormItems.get(0).getService(), is(ACC_EMERGENCY));
    assertThat(programDataFormItems.get(0).getUsageColumnsMap().getCode(), is("CONSUME_MALARIA"));
    assertThat(programDataFormItems.get(0).getValue(), is(20));
    assertThat(programDataFormItems.get(1).getService(), is(ACC_EMERGENCY));
    assertThat(programDataFormItems.get(1).getUsageColumnsMap().getCode(),
        is("POSITIVE_MALARIA"));
    assertThat(programDataFormItems.get(1).getValue(), is(1001));
    assertThat(programDataFormItems.get(2).getService(), is(ACC_EMERGENCY));
    assertThat(programDataFormItems.get(2).getUsageColumnsMap().getCode(),
        is("UNJUSTIFIED_MALARIA"));
    assertThat(programDataFormItems.get(2).getValue(), is(121));
  }

  @Test
  public void shouldReturnTestConsumptionsWhenConvertFormGridViewModelToDataModelWithDuoTest() {
    // given
    String consumeCode = "CONSUME_DUOTESTEHIVSIFILIS";
    duoTestViewModel.setConsumptionValue("20");
    UsageColumnsMap consumeColumn3 = new UsageColumnsMap();
    consumeColumn3.setCode(consumeCode);
    duoTestViewModel.setConsumeColumn(consumeColumn3);

    String positiveHivCode = "POSITIVEHIV_DUOTESTEHIVSIFILIS";
    duoTestViewModel.setPositiveHivValue("1000");
    UsageColumnsMap positiveHivColumn = new UsageColumnsMap();
    positiveHivColumn.setCode(positiveHivCode);
    duoTestViewModel.setPositiveHivColumn(positiveHivColumn);

    String positiveSyphilisCode = "POSITIVESYPHILIS_DUOTESTEHIVSIFILIS";
    duoTestViewModel.setPositiveSyphilisValue("1");
    UsageColumnsMap positiveSyphilisColumn = new UsageColumnsMap();
    positiveSyphilisColumn.setCode(positiveSyphilisCode);
    duoTestViewModel.setPositiveSyphilisColumn(positiveSyphilisColumn);

    String unjustifiedCode = "UNJUSTIFIED_DUOTESTEHIVSIFILIS";
    duoTestViewModel.setUnjustifiedValue("121");
    UsageColumnsMap unjustifiedCodeColumn = new UsageColumnsMap();
    unjustifiedCodeColumn.setCode(unjustifiedCode);
    duoTestViewModel.setUnjustifiedColumn(unjustifiedCodeColumn);

    MovementReasonManager.MovementReason reason = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.ISSUE, ACC_EMERGENCY, "Acc emergency");
    // when
    List<TestConsumptionItem> programDataFormItems =
        duoTestViewModel.convertFormGridViewModelToDataModel(reason);
    // then
    assertProgramDateFormItem(programDataFormItems.get(0), consumeCode, 20);
    assertProgramDateFormItem(programDataFormItems.get(1), positiveHivCode, 1000);
    assertProgramDateFormItem(programDataFormItems.get(2), positiveSyphilisCode, 1);
    assertProgramDateFormItem(programDataFormItems.get(3), unjustifiedCode, 121);
  }

  private void assertProgramDateFormItem(
      TestConsumptionItem testConsumptionItem, String columnCode, int value
  ) {
    assertEquals(ACC_EMERGENCY, testConsumptionItem.getService());
    assertEquals(columnCode, testConsumptionItem.getUsageColumnsMap().getCode());
    assertEquals(value, testConsumptionItem.getValue());
  }

  @Test
  public void shouldReturnTrueWhenIsDuoTestCalledByDuoTest() {
    assertTrue(duoTestViewModel.isDuoTest());
  }

  @Test
  public void shouldReturnTrueWhenIsDuoTestCalledByNonDuoTest() {
    assertFalse(viewModel.isDuoTest());
  }

  @Test
  public void shouldReturnTrueWhenIsEmptyCalledByDuoTestAndAllEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSIFILIS);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void shouldReturnTrueWhenIsEmptyCalledByDuoTestAndPositiveHivIsEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSIFILIS);
    duoTestViewModel.setPositiveSyphilisValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void shouldReturnTrueWhenIsEmptyCalledByDuoTestAndPositiveSyphilisIsEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSIFILIS);
    duoTestViewModel.setPositiveHivValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void shouldReturnFalseWhenIsEmptyCalledByDuoTestAndConsumptionIsNotEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSIFILIS);
    duoTestViewModel.setConsumptionValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldReturnFalseWhenIsEmptyCalledByDuoTestAndPositiveIsNotEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSIFILIS);
    duoTestViewModel.setPositiveHivValue(VALUE_50);
    duoTestViewModel.setPositiveSyphilisValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldReturnFalseWhenIsEmptyCalledByDuoTestAndUnjustifiedIsNotEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSIFILIS);
    duoTestViewModel.setUnjustifiedValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldSetPositiveHivAsInvalidColumnWhenThreeGridValidatedWithDuoTestAndPositiveSyphilisIsEmpty() {
    //given
    duoTestViewModel.setConsumptionValue("10");
    duoTestViewModel.setPositiveHivValue("10");
    duoTestViewModel.setUnjustifiedValue("10");

    // when
    MMITGridErrorType actualErrorType = duoTestViewModel.validateThreeGrid();

    //then
    assertEquals(EMPTY_POSITIVE, actualErrorType);
    assertEquals(POSITIVE_HIV, duoTestViewModel.invalidColumn);
  }

  @Test
  public void shouldSetPositiveHivAsInvalidColumnWhenThreeGridValidatedWithDuoTestAndPositiveHivIsEmpty() {
    //given
    duoTestViewModel.setConsumptionValue("10");
    duoTestViewModel.setPositiveSyphilisValue("10");
    duoTestViewModel.setUnjustifiedValue("10");

    // when
    MMITGridErrorType actualErrorType = duoTestViewModel.validateThreeGrid();

    //then
    assertEquals(EMPTY_POSITIVE, actualErrorType);
    assertEquals(POSITIVE_HIV, duoTestViewModel.invalidColumn);
  }

  @Test
  public void shouldSetPositiveHivAsInvalidColumnWhenThreeGridValidatedWithDuoTestAndPositiveIsGreaterThanConsume() {
    //given
    duoTestViewModel.setConsumptionValue("10");
    duoTestViewModel.setPositiveHivValue("10");
    duoTestViewModel.setPositiveSyphilisValue("10");
    duoTestViewModel.setUnjustifiedValue("10");

    // when
    MMITGridErrorType actualErrorType = duoTestViewModel.validateThreeGrid();

    //then
    assertEquals(POSITIVE_MORE_THAN_CONSUMPTION, actualErrorType);
    assertEquals(POSITIVE_HIV, duoTestViewModel.invalidColumn);
  }
}