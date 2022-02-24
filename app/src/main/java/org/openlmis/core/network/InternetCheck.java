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

package org.openlmis.core.network;

import android.util.Log;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InternetCheck {

  private static final String TAG = InternetCheck.class.getSimpleName();

  private static final int TIMEOUT = 5 * 1000;

  protected Observer<Boolean> resultObserver = new Observer<Boolean>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable throwable) {
      Log.w(TAG, throwable);
      listener.onResult(false);
    }

    @Override
    public void onNext(Boolean result) {
      listener.onResult(Boolean.TRUE == result);
    }
  };

  private InternetCheckListener listener;

  private static String getAddress() {
    return LMISApp.getContext().getString(R.string.server_base_url_host);
  }

  private static int getPORT() {
    return LMISApp.getContext().getResources().getInteger(R.integer.server_base_url_port);
  }

  public void check(InternetCheckListener listener) {
    this.listener = listener;
    Observable.create((OnSubscribe<Boolean>) subscriber -> {
      try (Socket sock = new Socket()) {
        sock.connect(new InetSocketAddress(getAddress(), getPORT()));
        subscriber.onNext(true);
      } catch (Exception e) {
        subscriber.onError(e);
      } finally {
        subscriber.onCompleted();
      }
    }).timeout(TIMEOUT, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(resultObserver);
  }
}