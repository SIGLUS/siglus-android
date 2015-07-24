package org.openlmis.core.activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.view.activity.LoginActivity;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.robolectric.Robolectric.application;

@RunWith(LMISTestRunner.class)
public class LoginActivityTest {

    private LoginActivity loginActivity;

    @Before
    public void setUp() {
        loginActivity = Robolectric.buildActivity(LoginActivity.class).create().get();
    }

    @Test
    public void shouldShowToastIfNothingInput() {
        loginActivity.btnLogin.performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), is(application.getResources().getString(R.string.msg_login_validate)));
    }
}