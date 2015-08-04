package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.MIMIAForm;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.repository.MIMIARepository;
import org.openlmis.core.view.View;

public class MIMIAFormPresenter implements Presenter{

    MIMIAForm form;
    MIMIAFormView view;

    @Inject
    MIMIARepository mimiaRepository;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) throws ViewNotMatchException{
        if (view instanceof MIMIAFormView){
            this.view = (MIMIAFormView)v;
        }else {
            throw new ViewNotMatchException(MIMIAFormView.class.getName());
        }
    }

    public MIMIAForm initMIMIA(){
        MIMIAForm form = null;
        try {
            form = mimiaRepository.initMIMIA();
        } catch (LMISException e){
            view.showErrorMessage(e.getMessage());
        }
        return form;
    }

    public void saveForm(){
        if (validate(form)){
            try {
                mimiaRepository.save(form);
            } catch (LMISException e){
                view.showErrorMessage(e.getMessage());
            }
        }
    }

    private boolean validate(MIMIAForm form){
        int totalRegimenNumber = 0;
        for (RegimenItem item : form.getRegimenItemList()){
            totalRegimenNumber += item.getAmount();
        }

        if (totalRegimenNumber != form.getTotalPatients()){
            return false;
        }

        return true;
    }

    public interface MIMIAFormView extends View {
        void showValidationAlert();
        void showErrorMessage(String msg);
    }
}
