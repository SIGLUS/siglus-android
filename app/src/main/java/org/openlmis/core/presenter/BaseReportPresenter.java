package org.openlmis.core.presenter;

import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.view.BaseView;

public class BaseReportPresenter extends Presenter{
    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
    }
}
