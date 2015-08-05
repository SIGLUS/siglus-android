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

package org.openlmis.core.view.activity;

import android.support.design.widget.TextInputLayout;
import android.widget.TextView;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
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
        assertThat(errorText.getText().toString(), is(loginActivity.getResources().getString(R.string.msg_empty_user)));
    }
}