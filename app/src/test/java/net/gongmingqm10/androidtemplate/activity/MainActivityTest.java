package net.gongmingqm10.androidtemplate.activity;

import android.app.AlertDialog;

import net.gongmingqm10.androidtemplate.TemplateTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowAlertDialog;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TemplateTestRunner.class)
public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() {
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
    }

    @Test
    public void shouldShowTwoWhenSixDividedByTwo() {
        fillNumberField(6D, 2D);
        mainActivity.resultBtn.performClick();

        assertThat(mainActivity.resultLabel.getText().toString()).isEqualTo("The result is 3");
    }

    @Test
    public void shouldShowAlertDialogWhenDividedByZero() {
        fillNumberField(3.2, 0D);
        mainActivity.resultBtn.performClick();

        ShadowAlertDialog dialog = Robolectric.getShadowApplication().getLatestAlertDialog();
        assertThat(dialog).isNotNull();
        assertThat(dialog.getMessage()).isEqualTo("The second number should not be zero");
    }

    @Test
    public void shouldDisableButtonWhenFieldIsNotFilled() {
        mainActivity.firstNumberEdit.setText("4");
        assertThat(mainActivity.resultBtn.isEnabled()).isFalse();

        mainActivity.secondNumberEdit.setText("5");

        assertThat(mainActivity.resultBtn.isEnabled()).isTrue();
    }

    @Test
    public void shouldClearSecondNumberWhenSecondNumberIsZero() {
        fillNumberField(3D, 0D);

        assertThat(mainActivity.secondNumberEdit.getText().toString()).isNotEmpty();
        mainActivity.resultBtn.performClick();

        assertThat(mainActivity.secondNumberEdit.getText().toString()).isEmpty();
    }


    private void fillNumberField(Double firstNumber, Double secondNumber) {
        mainActivity.firstNumberEdit.setText(String.valueOf(firstNumber));
        mainActivity.secondNumberEdit.setText(String.valueOf(secondNumber));
    }

}