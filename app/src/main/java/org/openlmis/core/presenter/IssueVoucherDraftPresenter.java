package org.openlmis.core.presenter;

import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;

import android.content.Intent;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseModel;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class IssueVoucherDraftPresenter extends Presenter {

  @Getter
  private final List<IssueVoucherProductViewModel> currentViewModels = new ArrayList<>();

  private IssueVoucherDraftView issueVoucherDraftView;
  @Inject
  private StockRepository stockRepository;

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {
    this.issueVoucherDraftView = (IssueVoucherDraftView) v;
  }

  public void initialViewModels(Intent intent) {
    issueVoucherDraftView.loading();
    currentViewModels.clear();
    Observable<List<IssueVoucherProductViewModel>> initObservable = getObservableFromProducts(
        (List<Product>) intent.getSerializableExtra(SELECTED_PRODUCTS));
    Subscription subscription = initObservable
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(viewModelsSubscribe);
    subscriptions.add(subscription);
  }

  private Observable<List<IssueVoucherProductViewModel>> getObservableFromProducts(List<Product> products) {
    return Observable.create(subscriber -> {
      try {
        ImmutableList<Long> productIds = FluentIterable.from(products).transform(BaseModel::getId).toList();
        List<StockCard> stockCards = stockRepository.listStockCardsByProductIds(productIds);
        final Map<Product, StockCard> productStockCardMap = new HashMap<>();
        for (StockCard stockCard : stockCards) {
          productStockCardMap.put(stockCard.getProduct(), stockCard);
        }
        List<IssueVoucherProductViewModel> issueVoucherProductViewModels = new ArrayList<>();
        for (Product product : products) {
          IssueVoucherProductViewModel issueVoucherProductViewModel;
          if (productStockCardMap.get(product) != null) {
            issueVoucherProductViewModel = new IssueVoucherProductViewModel(productStockCardMap.get(product));
          } else {
            issueVoucherProductViewModel = new IssueVoucherProductViewModel(product);
          }
          issueVoucherProductViewModels.add(issueVoucherProductViewModel);
        }
        subscriber.onNext(issueVoucherProductViewModels);
        subscriber.onCompleted();
      } catch (LMISException e) {
        new LMISException(e, "add products failed").reportToFabric();
        subscriber.onError(e);
      }
    });
  }

  private Observer<List<IssueVoucherProductViewModel>> viewModelsSubscribe = new Observer<List<IssueVoucherProductViewModel>>() {
    @Override
    public void onCompleted() {
      issueVoucherDraftView.loaded();
      issueVoucherDraftView.onRefreshViewModels();
    }

    @Override
    public void onError(Throwable e) {
      issueVoucherDraftView.loaded();
      issueVoucherDraftView.onLoadViewModelsFailed(e);
    }

    @Override
    public void onNext(List<IssueVoucherProductViewModel> issueVoucherProductViewModels) {
      currentViewModels.addAll(issueVoucherProductViewModels);
    }
  };

  public interface IssueVoucherDraftView extends BaseView {

    void onRefreshViewModels();

    void onLoadViewModelsFailed(Throwable throwable);
  }

}
