package net.gongmingqm10.androidtemplate.activity;

import net.gongmingqm10.androidtemplate.TemplateTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

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
        mainActivity.firstNumberEdit.setText("6");
        mainActivity.secondNumberEdit.setText("2");

        mainActivity.resultBtn.performClick();

        assertThat(mainActivity.resultLabel.getText().toString()).isEqualTo("Result is 3");
    }

}