package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddDrugsToVIAPresenter extends Presenter {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private ProgramRepository programRepository;

    @Inject
    private ProductProgramRepository productProgramRepository;

    @Inject
    private RnrFormItemRepository rnrFormItemRepository;

    AddDrugsToVIAView view;

    @Override
    public void attachView(BaseView v) {
        view = (AddDrugsToVIAView) v;
    }

    public Observable<List<InventoryViewModel>> loadActiveProductsNotInVIAForm() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    final List<String> viaProgramCodes = programRepository.queryProgramCodesByProgramCodeOrParentCode(Constants.VIA_PROGRAM_CODE);
                    List<Long> allActiveVIAProductIds = productProgramRepository.queryActiveProductIdsByProgramsWithKits(viaProgramCodes, false);
                    final List<Long> allExistingProductsInCurrentVIADraft = rnrFormItemRepository.listAllProductIdsInCurrentVIADraft();
                    final List<Long> productsNewlyAddedAsRnrItems = rnrFormItemRepository.listAllProductIdsNewlyAddedAsRnrItems();

                    List<InventoryViewModel> productsNotInVIAForm = FluentIterable.from(allActiveVIAProductIds).filter(new Predicate<Long>() {
                        @Override
                        public boolean apply(Long productId) {
                            return !allExistingProductsInCurrentVIADraft.contains(productId) && !productsNewlyAddedAsRnrItems.contains(productId);
                        }
                    }).transform(new Function<Long, InventoryViewModel>() {
                        @Override
                        public InventoryViewModel apply(Long productIdToAdd) {
                            return new InventoryViewModel(productRepository.getById(productIdToAdd));
                        }
                    }).toList();

                    subscriber.onNext(productsNotInVIAForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public ArrayList<RnrFormItem> generateNewVIAItems(List<InventoryViewModel> checkedViewModels) throws LMISException {

        if (!view.validateInventory()) {
            return null;
        }
        List<RnrFormItem> rnrFormItemList = FluentIterable.from(checkedViewModels).transform(new Function<InventoryViewModel, RnrFormItem>() {
            @Override
            public RnrFormItem apply(InventoryViewModel inventoryViewModel) {
                RnrFormItem rnrFormItem = new RnrFormItem();
                rnrFormItem.setProduct(inventoryViewModel.getProduct());
                rnrFormItem.setRequestAmount(Long.parseLong(inventoryViewModel.getQuantity()));
                return rnrFormItem;
            }
        }).toList();
        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);
        return new ArrayList(rnrFormItemList);
    }

    public interface AddDrugsToVIAView extends BaseView {
        boolean validateInventory();
    }
}
