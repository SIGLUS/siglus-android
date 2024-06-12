package org.openlmis.core.view.viewmodel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
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

  RapidTestFormGridViewModel duoTestViewModel = new RapidTestFormGridViewModel(
      ColumnCode.DUOTESTEHIVSYPHILIS);
  private static String VALUE_50 = "50";

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
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSYPHILIS);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void shouldReturnTrueWhenIsEmptyCalledByDuoTestAndPositiveHivIsEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSYPHILIS);
    duoTestViewModel.setPositiveSyphilisValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void shouldReturnTrueWhenIsEmptyCalledByDuoTestAndPositiveSyphilisIsEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSYPHILIS);
    duoTestViewModel.setPositiveHivValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertTrue(actualResult);
  }

  @Test
  public void shouldReturnFalseWhenIsEmptyCalledByDuoTestAndConsumptionIsNotEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSYPHILIS);
    duoTestViewModel.setConsumptionValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldReturnFalseWhenIsEmptyCalledByDuoTestAndPositiveIsNotEmpty() {
    // given
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSYPHILIS);
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
    duoTestViewModel = new RapidTestFormGridViewModel(ColumnCode.DUOTESTEHIVSYPHILIS);
    duoTestViewModel.setUnjustifiedValue(VALUE_50);
    // when
    boolean actualResult = duoTestViewModel.isEmpty();
    // then
    assertFalse(actualResult);
  }
}