package org.openlmis.core.presenter;

public interface VIARequisitionView extends BaseRequisitionPresenter.BaseRequisitionView {

  void showListInputError(int index);

  void highLightRequestAmount();

  void highLightApprovedAmount();

  void setProcessButtonName(String name);

  boolean validateConsultationNumber();
}
