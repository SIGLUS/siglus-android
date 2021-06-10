package org.openlmis.core.view.fragment.builders;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.fragment.WarningDialogFragment;

@RunWith(LMISTestRunner.class)
public class WarningDialogFragmentBuilderTest {

  @Test
  public void shouldBuildAWarningDialogGivenADelegateAndMessages() {
    WarningDialogFragmentBuilder warningDialogFragmentBuilder = new WarningDialogFragmentBuilder();

    WarningDialogFragment.DialogDelegate delegate = new WarningDialogFragment.DialogDelegate() {
      @Override
      public void onPositiveClick() {

      }
    };
    int titleMessage = 2;
    int positiveButtonMessage = 3;
    int negativeButtonMessage = 4;

    WarningDialogFragment warningDialogFragment = warningDialogFragmentBuilder.build(delegate,
        titleMessage, positiveButtonMessage, negativeButtonMessage);

    assertThat(warningDialogFragment.getArguments().get("messageResId"),
        is(titleMessage));
    assertThat(warningDialogFragment.getArguments().get("positiveTextResId"),
        is(positiveButtonMessage));
    assertThat(warningDialogFragment.getArguments().get("negativeTextResId"),
        is(negativeButtonMessage));
  }


}