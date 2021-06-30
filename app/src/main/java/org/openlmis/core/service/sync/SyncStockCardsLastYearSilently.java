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

package org.openlmis.core.service.sync;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.utils.DateUtil;
import rx.Observable;
import rx.Scheduler;

public class SyncStockCardsLastYearSilently {


  private static final int DAYS_OF_REGULAR_MONTH = 30;

  @Inject
  private SharedPreferenceMgr sharedPreferenceMgr;
  private LMISRestApi lmisRestApi;
  private String facilityId;
  private Scheduler scheduler;

  public Observable<List<StockCard>> performSync() {
    final int monthsInAYear = 12;
    lmisRestApi = LMISApp.getInstance().getRestApi();
    facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
    List<Observable<StockCardsLocalResponse>> tasks = new ArrayList<>();
    scheduler = SchedulerBuilder.createScheduler();
    int startMonth = sharedPreferenceMgr.getPreference()
        .getInt(SharedPreferenceMgr.KEY_STOCK_SYNC_CURRENT_INDEX, 1);
    Date now = getActualDate();

    for (int month = startMonth; month <= monthsInAYear; month++) {
      Observable<StockCardsLocalResponse> objectObservable =
          createObservableToFetchStockMovements(month, now);
      tasks.add(objectObservable);
    }
    return zipObservables(tasks);
  }

  @NonNull
  private Observable<List<StockCard>> zipObservables(List<Observable<StockCardsLocalResponse>> tasks) {
    return Observable.zip(tasks, args -> {
      List<StockCard> stockCards = new ArrayList<>();
      for (Object object : args) {
        stockCards.addAll(((StockCardsLocalResponse) object).getStockCards());
      }
      return stockCards;
    });
  }

  private Observable<StockCardsLocalResponse> createObservableToFetchStockMovements(int month, Date now) {
    final String startDateStr = getStartDate(now, month);
    final String endDateStr = getEndDate(now, month);
    return Observable.create((Observable.OnSubscribe<StockCardsLocalResponse>) subscriber -> {
      try {
        StockCardsLocalResponse adaptedResponse = lmisRestApi
            .fetchStockMovementData(facilityId, startDateStr, endDateStr);
        subscriber.onNext(adaptedResponse);
        subscriber.onCompleted();
      } catch (LMISException e) {
        Log.w("SyncStockCardLastYear", e);
        subscriber.onError(e);
      }
    }).subscribeOn(scheduler);
  }

  private String getStartDate(Date now, int month) {
    final int oneMonth = 1;
    Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_REGULAR_MONTH * (month + oneMonth));
    return DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);
  }

  private String getEndDate(Date now, int month) {
    Date endDate = DateUtil.minusDayOfMonth(now, DAYS_OF_REGULAR_MONTH * month);
    return DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);
  }

  @NonNull
  private Date getActualDate() {
    long syncEndTimeMillions = sharedPreferenceMgr.getPreference()
        .getLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, DateUtil.getCurrentDate().getTime());
    return new Date(syncEndTimeMillions);
  }
}
