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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.repository.VIAReposotory;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RequisitionPresenter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_requisition)
public class RequisitionActivity extends BaseActivity implements RequisitionPresenter.RequisitionView{

    @Inject
    RequisitionPresenter presenter;

    @InjectView(R.id.requisition_form)
    WebView requisitionForm;

    @Inject
    VIAReposotory viaReposotory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requisitionForm.getSettings().setJavaScriptEnabled(true);
        requisitionForm.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }

        });

        requisitionForm.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                startLoading();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                stopLoading();
            }


        });

        requisitionForm.loadUrl("file:///android_asset/www/form.html");
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
