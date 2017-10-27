package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.builders.PTVProgramBuilder;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.Period;
import org.openlmis.core.view.BaseView;

import lombok.Setter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PtvProgramPresenter extends Presenter {

    @Inject
    private PTVProgramBuilder ptvProgramBuilder;

    @Setter
    private Period period;

    public PtvProgramPresenter() {
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {

    }

    public Observable<PTVProgram> buildInitialPtvProgram() {
        return Observable.create(new Observable.OnSubscribe<PTVProgram>() {
            @Override
            public void call(Subscriber<? super PTVProgram> subscriber) {
                try {
                    if(period==null){
                        throw new LMISException("Period cannot be null");
                    }
                    PTVProgram ptvProgram = ptvProgramBuilder.buildPTVProgram(period);
                    subscriber.onNext(ptvProgram);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }
}
