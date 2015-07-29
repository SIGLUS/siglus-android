package org.openlmis.core.activity;

import android.support.design.widget.TextInputLayout;
import android.widget.TextView;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.view.activity.LoginActivity;
import org.robolectric.Robolectric;

import java.lang.reflect.Field;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(LMISTestRunner.class)
public class LoginActivityTest {

    private LoginActivity loginActivity;

    @Before
    public void setUp() {
        loginActivity = Robolectric.buildActivity(LoginActivity.class).create().get();
    }

    @Test
    public void shouldShowErrorWhenUserNameIsEmpty(){
        loginActivity.btnLogin.performClick();

        TextView errorText = null;

        Field field = FieldUtils.getField(TextInputLayout.class, "mErrorView", true);
        try {
            errorText = (TextView)field.get(loginActivity.lyUserName);
        }catch (IllegalAccessException e){

        }

        assertThat(errorText, notNullValue());
        assertThat(errorText.getText().toString(), is(loginActivity.getResources().getString(R.string.msg_login_validate)));
    }
}