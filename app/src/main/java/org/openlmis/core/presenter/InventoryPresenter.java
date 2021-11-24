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


package org.openlmis.core.presenter;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.adapter.BulkInitialInventoryAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class InventoryPresenter extends Presenter {

  @Inject
  ProductRepository productRepository;

  @Inject
  ProgramRepository programRepository;

  @Inject
  StockRepository stockRepository;

  @Inject
  InventoryRepository inventoryRepository;

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  InventoryView view;

  @Getter
  List<Program> programs;

  @Getter
  final List<InventoryViewModel> inventoryViewModelList = new ArrayList<>();

  @Override
  public void attachView(BaseView v) {
    view = (InventoryView) v;
  }

  public abstract Observable<List<InventoryViewModel>> loadInventory();

  public Observable<List<InventoryViewModel>> getInflatedInventoryOnMainThread() {
    return getInflatedInventory()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io());
  }

  public Observable<List<InventoryViewModel>> getInflatedInventory() {
    programs = programRepository.queryProgramWithoutML();
    Map<String, Program> codeToProgram = new HashMap<>();
    for (Program program : programs) {
      codeToProgram.put(program.getProgramCode(), program);
    }
    Map<String, String> productCodeToProgram = productRepository.listProductCodeToProgramCode();
    return loadInventory().doOnNext(inventoryViewModels -> {
      for (InventoryViewModel inventoryViewModel : inventoryViewModels) {
        if (inventoryViewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_BASIC_HEADER
            || inventoryViewModel.getViewType() == BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER) {
          continue;
        }
        inventoryViewModel
            .setProgram(codeToProgram.get(productCodeToProgram.get(inventoryViewModel.getProduct().getCode())));
      }
    });
  }

  public interface InventoryView extends BaseView {

    boolean validateInventory();

    void showErrorMessage(String msg);
  }
}
