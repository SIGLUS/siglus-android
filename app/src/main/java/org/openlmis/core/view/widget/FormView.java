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

package org.openlmis.core.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.exceptions.LMISException;

public class FormView extends WebView {

    public static final String JS_INTERFACE = "JSInterface";

    private FormViewCallback callback;

    public FormView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initWebView();
    }

    private void initWebView() {
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(this, JS_INTERFACE);
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", consoleMessage.message());
                if (callback != null) {
                    callback.onError(new LMISException(consoleMessage.message()));
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });


        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (callback != null) {
                    callback.onStartLoading();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (callback != null) {
                    callback.onStopLoading();
                }
            }
        });
    }

    public void loadForm(String formName){
        loadUrl("file:///android_asset/forms/" + formName + ".html");
    }

    public void setFormViewListener(FormViewCallback callback) {
        this.callback = callback;
    }

    @JavascriptInterface
    public String fillFormData(){
        if (callback != null){
            return callback.fillFormData();
        }
        return StringUtils.EMPTY;
    }

    public interface FormViewCallback {
        void onStartLoading();
        void onStopLoading();
        void onError(LMISException e);

        String fillFormData();
    }
}
