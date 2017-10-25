package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.HealthFacilityServiceRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PtvProgramPresenter extends Presenter{

    @Inject
    HealthFacilityServiceRepository healthFacilityServiceRepository;

    @Inject
    StockRepository stockRepository;

    public Observable<List<HealthFacilityService>> getAllHealthFacilityServices() throws LMISException {
        return Observable.create(new Observable.OnSubscribe<List<HealthFacilityService>>() {
            @Override
            public void call(Subscriber<? super List<HealthFacilityService>> subscriber) {
                try {
                    subscriber.onNext(healthFacilityServiceRepository.getAll());
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public List<PTVProgramStockInformation> generatePTVProgramStockInformation(List<String> ptvProductCodes) throws LMISException {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>();
        for (String code: ptvProductCodes) {
            StockCard stockCard = stockRepository.queryStockCardByProductCode(code);
            List<StockMovementItem> stockMovementItems = ((List<StockMovementItem>) stockCard.getForeignStockMovementItems());
            PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
            ptvProgramStockInformation.setProduct(stockCard.getProduct());
            ptvProgramStockInformation.setInitialStock((int) stockCard.getStockOnHand());
            ptvProgramStockInformation.setPtvProgram(new PTVProgram());
            for (StockMovementItem movementItem : stockMovementItems) {
                if (movementItem.getMovementType().equals(MovementReasonManager.MovementType.RECEIVE)) {
                    ptvProgramStockInformation.setEntries((int) movementItem.getMovementQuantity());
                }
            }
            ptvProgramStocksInformation.add(ptvProgramStockInformation);
        }
        return ptvProgramStocksInformation;
    }
}
