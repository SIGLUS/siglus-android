package org.openlmis.core.presenter;

import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.view.BaseView;

public class AllDrugsMovementPresenter extends Presenter{
    AllDrugsMovementView view;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (AllDrugsMovementView) v;
    }

    public void loadAllMovementHistory() {

    }

    public interface AllDrugsMovementView extends BaseView{
    }
}
