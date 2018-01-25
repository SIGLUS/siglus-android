package org.openlmis.core.view.fragment.builders;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;

@RunWith(LMISTestRunner.class)
public class WarningDialogFragmentBuilderTest {

    @Test
    public void shouldBuildAWarningDialogGivenADelegateAndMessages(){
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

        assertThat((Integer) warningDialogFragment.getArguments().get("messageResId"),
                is(titleMessage));
        assertThat((Integer) warningDialogFragment.getArguments().get("positiveTextResId"),
                is(positiveButtonMessage));
        assertThat((Integer) warningDialogFragment.getArguments().get("negativeTextResId"),
                is(negativeButtonMessage));
    }



}