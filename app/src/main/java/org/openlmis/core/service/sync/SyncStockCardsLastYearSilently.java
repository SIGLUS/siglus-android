package org.openlmis.core.service.sync;

import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

public class SyncStockCardsLastYearSilently {


    private final int DAYS_OF_REGULAR_MONTH = 30;

    @Inject
    private SharedPreferenceMgr sharedPreferenceMgr;

    private LMISRestApi lmisRestApi;
    private String facilityId;
    private Scheduler scheduler;

    @Inject
    public SyncStockCardsLastYearSilently() {
    }

    public Observable<List<StockCard>> performSync() {
        final int monthsInAYear = 12;
        lmisRestApi = LMISApp.getInstance().getRestApi();
        facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
        List<Observable<SyncDownStockCardResponse>> tasks = new ArrayList<>();
        scheduler = createScheduler();
        int startMonth = sharedPreferenceMgr.getPreference().getInt(SharedPreferenceMgr.KEY_STOCK_SYNC_CURRENT_INDEX, 1);

        for (int month = startMonth; month <= monthsInAYear; month++) {
            Observable<SyncDownStockCardResponse> objectObservable = createObservableToFetchStockMovements(month);
            tasks.add(objectObservable);
        }

        return zipObservables(tasks);
    }

    @NonNull
    private Observable<List<StockCard>> zipObservables(List<Observable<SyncDownStockCardResponse>> tasks) {
        return Observable.zip(tasks, new FuncN<List<StockCard>>() {
            @Override
            public List<StockCard> call(Object... args) {
                List<StockCard> stockCards = new ArrayList<>();
                for (Object object : args) {
                    stockCards.addAll(((SyncDownStockCardResponse) object).getStockCards());
                }

                return stockCards;
            }
        });
    }

    private Observable<SyncDownStockCardResponse> createObservableToFetchStockMovements(int month) {
        Date now = getActualDate();
        final String startDateStr = getStartDate(now, month);
        final String endDateStr = getEndDate(now, month);

        return Observable.create(new Observable.OnSubscribe<SyncDownStockCardResponse>() {
            @Override
            public void call(Subscriber<? super SyncDownStockCardResponse> subscriber) {
                try {
                    SyncDownStockCardResponse syncDownStockCardResponse = lmisRestApi.fetchStockMovementData(facilityId, startDateStr, endDateStr);
                    subscriber.onNext(syncDownStockCardResponse);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
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
        long syncEndTimeMillions = sharedPreferenceMgr.getPreference().getLong(SharedPreferenceMgr.KEY_STOCK_SYNC_END_TIME, new Date().getTime());
        return new Date(syncEndTimeMillions);
    }

    @NonNull
    private Scheduler createScheduler() {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadCount);
        return Schedulers.from(threadPoolExecutor);
    }
}
