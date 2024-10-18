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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.util.Log;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.DraftBulkEntriesProduct;
import org.openlmis.core.model.DraftBulkEntriesProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.BulkEntriesRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.activity.BulkEntriesActivity;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.Iterables;
import org.roboguice.shaded.goole.common.collect.Lists;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BulkEntriesPresenter extends Presenter {

  private static final String TAG = BulkEntriesPresenter.class.getSimpleName();
  @Inject
  private final StockRepository stockRepository;
  @Inject
  private final ProductRepository productRepository;
  @Inject
  private final BulkEntriesRepository bulkEntriesRepository;
  @Getter
  private final List<BulkEntriesViewModel> bulkEntriesViewModels;

  @Inject
  public BulkEntriesPresenter(StockRepository stockRepository,
      ProductRepository productRepository,
      BulkEntriesRepository bulkEntriesRepository) {
    this.stockRepository = stockRepository;
    this.productRepository = productRepository;
    this.bulkEntriesRepository = bulkEntriesRepository;
    this.bulkEntriesViewModels = new ArrayList<>();
  }

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  public Observable<List<BulkEntriesViewModel>> getBulkEntriesViewModelsFromDraft() {
    return Observable
        .create((Observable.OnSubscribe<List<BulkEntriesViewModel>>) subscriber -> {
          try {
            restoreDraftInventory();
            subscriber.onNext(bulkEntriesViewModels);
            subscriber.onCompleted();
          } catch (Exception e) {
            subscriber.onError(e);
          }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  public List<String> getAddedProductCodes() {
    return from(bulkEntriesViewModels).transform(viewModel -> viewModel.getProduct().getCode())
        .toList();
  }

  public void addNewProductsToBulkEntriesViewModels(List<Product> addedProducts) {
    bulkEntriesViewModels.addAll(
        from(addedProducts).transform(this::convertProductToBulkEntriesViewModel)
            .toList());
  }

  public Observable<Object> saveDraftBulkEntriesObservable() {
    return Observable.create(subscriber -> {
      try {
        bulkEntriesRepository.clearBulkEntriesDraft();
        for (BulkEntriesViewModel bulkEntriesViewModel : bulkEntriesViewModels) {
          bulkEntriesRepository.createBulkEntriesProductDraft(
              new DraftBulkEntriesProduct(bulkEntriesViewModel));
        }
        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        Log.w(TAG, e);
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  public void deleteDraft() {
    try {
      bulkEntriesRepository.clearBulkEntriesDraft();
    } catch (LMISException e) {
      Log.w(TAG, e);
    }
  }

  public boolean isDraftExisted() {
    List<DraftBulkEntriesProduct> draftBulkEntriesProducts = new ArrayList<>();
    try {
      draftBulkEntriesProducts = bulkEntriesRepository.queryAllBulkEntriesDraft();
    } catch (LMISException e) {
      Log.w(TAG, e);
    }
    return !(draftBulkEntriesProducts.isEmpty() && bulkEntriesViewModels.isEmpty());
  }

  public Observable<Long> saveBulkEntriesProducts(String signature) {
    return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
      try {
        long createdTime = LMISApp.getInstance().getCurrentTimeMillis();
        for (BulkEntriesViewModel bulkEntriesViewModel : bulkEntriesViewModels) {
          bulkEntriesViewModel.setSignature(signature);
          bulkEntriesViewModel.calculateLotOnHand();
          bulkEntriesViewModel.setDefaultReasonForNoAmountLot(Constants.DEFAULT_REASON_FOR_NO_AMOUNT_LOT);
          StockCard stockCard = new StockCard();
          if (bulkEntriesViewModel.getStockCard() != null) {
            stockCard = bulkEntriesViewModel.getStockCard();
          } else {
            stockCard.setProduct(bulkEntriesViewModel.getProduct());
            StockMovementItem initialStockMovementItem = stockCard.generateInitialStockMovementItem();
            stockRepository.addStockMovementAndUpdateStockCard(initialStockMovementItem, createdTime);
          }
          stockCard.getProduct().setArchived(false);
          stockCard.setStockOnHand(getStockOnHand(bulkEntriesViewModel));
          productRepository.updateProduct(stockCard.getProduct());
          StockMovementItem stockMovementItem = buildStockMovementItem(bulkEntriesViewModel, stockCard);
          stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem, (createdTime + 1));
          Long stockCardId = stockRepository.queryStockCardByProductId(bulkEntriesViewModel.getProductId()).getId();
          subscriber.onNext(stockCardId);
        }
      } catch (LMISException e) {
        new LMISException(e, "BulkEntriesPresenter.saveBulkEntriesProducts").reportToFabric();
      }
      subscriber.onCompleted();
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  protected void restoreDraftInventory() throws LMISException {
    List<DraftBulkEntriesProduct> draftBulkEntriesProducts = bulkEntriesRepository.queryAllBulkEntriesDraft();
    bulkEntriesViewModels.addAll(FluentIterable.from(draftBulkEntriesProducts).transform(
        draftBulkEntriesProduct -> new BulkEntriesViewModel(
            Objects.requireNonNull(draftBulkEntriesProduct).getProduct(),
            draftBulkEntriesProduct.isDone(), draftBulkEntriesProduct.getQuantity(),
            convertDraftLotMovementToLotMovement(draftBulkEntriesProduct.getDraftLotItemListWrapper()))
    ).transform(this::setLotViewModels).toList());
  }

  private StockMovementItem buildStockMovementItem(BulkEntriesViewModel bulkEntriesViewModel, StockCard stockCard) {
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementDate(DateUtil.getCurrentDate());
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.setMovementType(MovementType.RECEIVE);
    stockMovementItem.setReason("DISTRICT_DDM");
    stockMovementItem.setMovementQuantity(bulkEntriesViewModel.getLotListQuantityTotalAmount());
    stockMovementItem.setStockOnHand(stockCard.getStockOnHand());
    stockMovementItem.setSignature(bulkEntriesViewModel.getSignature());
    stockMovementItem.populateLotAndResetStockOnHandOfLotAccordingPhysicalAdjustment(
        bulkEntriesViewModel.getExistingLotMovementViewModelList(),
        bulkEntriesViewModel.getNewLotMovementViewModelList());
    return stockMovementItem;
  }


  private Long getStockOnHand(BulkEntriesViewModel bulkEntriesViewModel) {
    List<LotMovementViewModel> lotMovementViewModelList = Lists.newArrayList(Iterables
        .concat(bulkEntriesViewModel.getExistingLotMovementViewModelList(),
            bulkEntriesViewModel.getNewLotMovementViewModelList()));
    long stockOnHand = 0L;
    for (LotMovementViewModel lotMovementViewModel : lotMovementViewModelList) {
      stockOnHand += Long.parseLong(lotMovementViewModel.getLotSoh());
    }
    return stockOnHand;
  }

  private BulkEntriesViewModel setLotViewModels(BulkEntriesViewModel bulkEntriesViewModel) {
    StockCard stockCard = null;
    try {
      stockCard = stockRepository.queryStockCardByProductId(bulkEntriesViewModel.getProductId());
    } catch (LMISException e) {
      Log.e(TAG, e.toString());
    }
    if (stockCard == null) {
      bulkEntriesViewModel.setNewLotMovementViewModelList(
          new ArrayList<>(bulkEntriesViewModel.getLotMovementViewModels()));
    } else {
      getLotFromLotOnHand(bulkEntriesViewModel, stockCard);
    }
    return bulkEntriesViewModel;
  }

  private void getLotFromLotOnHand(BulkEntriesViewModel bulkEntriesViewModel, StockCard stockCard) {
    bulkEntriesViewModel.setStockCard(stockCard);
    List<LotMovementViewModel> lotMovementViewModels = getExistingLotMovementViewModels(
        bulkEntriesViewModel);
    List<String> lotNumbers = FluentIterable.from(lotMovementViewModels)
        .transform(LotMovementViewModel::getLotNumber)
        .toList();
    bulkEntriesViewModel.setExistingLotMovementViewModelList(getExistingLotMovementViewModels(
        bulkEntriesViewModel));
    setDraftToLotList(bulkEntriesViewModel, lotNumbers);
  }

  private void setDraftToLotList(BulkEntriesViewModel bulkEntriesViewModel,
      List<String> lotNumbers) {
    for (LotMovementViewModel draftLotMovementViewModel : bulkEntriesViewModel
        .getLotMovementViewModels()) {
      if (draftLotMovementViewModel.isNewAdded() && !lotNumbers
          .contains(draftLotMovementViewModel.getLotNumber())) {
        bulkEntriesViewModel.getNewLotMovementViewModelList().add(draftLotMovementViewModel);
      }
      if (lotNumbers.contains(draftLotMovementViewModel.getLotNumber())) {
        setDraftToExistingLotWhenHasTheSameLot(bulkEntriesViewModel, draftLotMovementViewModel);
      }
    }
  }

  private void setDraftToExistingLotWhenHasTheSameLot(BulkEntriesViewModel bulkEntriesViewModel,
      LotMovementViewModel draftLotMovementViewModel) {
    for (LotMovementViewModel existingMovementViewModel : bulkEntriesViewModel
        .getExistingLotMovementViewModelList()) {
      if (existingMovementViewModel.getLotNumber()
          .equals(draftLotMovementViewModel.getLotNumber())) {
        existingMovementViewModel.setQuantity(draftLotMovementViewModel.getQuantity());
        existingMovementViewModel
            .setDocumentNumber(draftLotMovementViewModel.getDocumentNumber());
        existingMovementViewModel
            .setMovementReason(draftLotMovementViewModel.getMovementReason());
      }
    }
  }

  private BulkEntriesViewModel convertProductToBulkEntriesViewModel(Product product) {
    try {
      BulkEntriesViewModel viewModel;
      StockCard stockCard = stockRepository.queryStockCardByProductId(product.getId());
      if (stockCard != null) {
        viewModel = new BulkEntriesViewModel(stockCard);
      } else {
        viewModel = new BulkEntriesViewModel(product);
      }
      viewModel.setChecked(false);
      setExistingLotViewModels(viewModel);
      return viewModel;
    } catch (LMISException e) {
      new LMISException(e, "BulkEntriesPresenter.convertProductToBulkEntriesViewModel")
          .reportToFabric();
    }
    return null;
  }

  private List<LotMovementViewModel> convertDraftLotMovementToLotMovement(
      List<DraftBulkEntriesProductLotItem> draftBulkEntriesProductLotItems) {
    return FluentIterable.from(draftBulkEntriesProductLotItems)
        .transform(draftBulkEntriesProductLotItem -> LotMovementViewModel.builder()
            .documentNumber(draftBulkEntriesProductLotItem.getDocumentNumber())
            .lotNumber(draftBulkEntriesProductLotItem.getLotNumber())
            .expiryDate(DateUtil.formatDate(draftBulkEntriesProductLotItem.getExpirationDate(),
                DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
            .movementReason(draftBulkEntriesProductLotItem.getReason())
            .quantity(getString(draftBulkEntriesProductLotItem.getQuantity()))
            .lotSoh(getString(draftBulkEntriesProductLotItem.getLotSoh()))
            .valid(true)
            .build()).toList();
  }

  private void setExistingLotViewModels(BulkEntriesViewModel bulkEntriesViewModel) {
    if (bulkEntriesViewModel.getStockCard() == null) {
      return;
    }
    bulkEntriesViewModel.setExistingLotMovementViewModelList(
        getExistingLotMovementViewModels(bulkEntriesViewModel));
  }

  private List<LotMovementViewModel> getExistingLotMovementViewModels(
      BulkEntriesViewModel bulkEntriesViewModel) {
    return FluentIterable
        .from(bulkEntriesViewModel.getStockCard().getNonEmptyLotOnHandList())
        .transform(lotOnHand -> new LotMovementViewModel(Objects.requireNonNull(lotOnHand).getLot().getLotNumber(),
            DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(),
                DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
            lotOnHand.getQuantityOnHand().toString(),
            MovementReasonManager.MovementType.RECEIVE))
        .transform(
            lotMovementViewModel -> Objects.requireNonNull(lotMovementViewModel)
                .setFrom(BulkEntriesActivity.KEY_FROM_BULK_ENTRIES_COMPLETE))
        .toSortedList((lot1, lot2) -> {
          Date localDate = DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
          if (localDate != null) {
            return localDate.compareTo(DateUtil
                .parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
          } else {
            return 0;
          }
        });
  }

  private String getString(Long number) {
    return number == null ? null : number.toString();
  }
}
