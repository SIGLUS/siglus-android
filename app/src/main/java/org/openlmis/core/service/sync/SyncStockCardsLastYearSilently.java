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

import static org.openlmis.core.utils.Constants.STOCK_CARD_MAX_SYNC_MONTH;

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.LMISApp;
import org.openlmis.core.event.SyncPercentEvent;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.utils.DateUtil;
import rx.Observable;
import rx.Scheduler;

public class SyncStockCardsLastYearSilently {

  private static final int DAYS_OF_REGULAR_MONTH = 30;

  private LMISRestApi lmisRestApi;
  private Scheduler scheduler;
  private final AtomicInteger finishedCount = new AtomicInteger(0);
  private final SyncPercentEvent syncPercentEvent = new SyncPercentEvent(0);

  public Observable<List<StockCard>> performSync() {
    lmisRestApi = LMISApp.getInstance().getRestApi();
    List<Observable<StockCardsLocalResponse>> tasks = new ArrayList<>();
    scheduler = SchedulerBuilder.createScheduler();
    finishedCount.set(0);
    int startMonth = 1;
    Date now = DateUtil.getCurrentDate();
    for (int month = startMonth; month <= STOCK_CARD_MAX_SYNC_MONTH; month++) {
      Observable<StockCardsLocalResponse> objectObservable = createObservableToFetchStockMovements(month, now);
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
        StockCardsLocalResponse adaptedResponse = lmisRestApi.fetchStockMovementData(startDateStr, endDateStr);
        syncPercentEvent.setSyncedCount(finishedCount.incrementAndGet());
        EventBus.getDefault().post(syncPercentEvent);
        subscriber.onNext(adaptedResponse);
        subscriber.onCompleted();
      } catch (LMISException e) {
        Log.w("SyncStockCardLastYear", e);
        subscriber.onError(e);
      }
    }).subscribeOn(scheduler);
  }

  private String getStartDate(Date now, int month) {
    Date startDate = DateUtil.minusDayOfMonth(now, DAYS_OF_REGULAR_MONTH * (month + 1));
    return DateUtil.formatDate(startDate, DateUtil.DB_DATE_FORMAT);
  }

  private String getEndDate(Date now, int month) {
    Date endDate = DateUtil.minusDayOfMonth(now, DAYS_OF_REGULAR_MONTH * month);
    return DateUtil.formatDate(endDate, DateUtil.DB_DATE_FORMAT);
  }
}
