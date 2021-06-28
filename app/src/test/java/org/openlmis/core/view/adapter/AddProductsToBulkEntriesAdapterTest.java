package org.openlmis.core.view.adapter;


import static org.mockito.Mockito.verify;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.view.holder.AddProductsToBulkEntriesViewHolder;
import org.openlmis.core.view.viewmodel.ProductsToBulkEntriesViewModel;

@RunWith(LMISTestRunner.class)
public class AddProductsToBulkEntriesAdapterTest {

  private RecyclerView recyclerView;
  private Product product;
  private ProductsToBulkEntriesViewModel viewModel;

  @Before
  public void setUp() throws Exception {
    recyclerView = new RecyclerView(LMISTestApp.getContext());
    recyclerView.setLayoutManager(new LinearLayoutManager(LMISTestApp.getContext()));
    product = Product.builder()
        .primaryName("kit covid 2019")
        .isActive(true)
        .isArchived(false)
        .isBasic(true)
        .code("22A07")
        .isHiv(false)
        .isKit(false)
        .build();
  }

  @Test
  public void testOnCreateViewHolder() {
    // given
    final ArrayList<ProductsToBulkEntriesViewModel> viewModels = new ArrayList<>();
    final AddProductsToBulkEntriesAdapter adapter = new AddProductsToBulkEntriesAdapter(viewModels);
    recyclerView.setAdapter(adapter);

    // when
    final AddProductsToBulkEntriesViewHolder addProductsToBulkEntriesViewHolder = adapter.onCreateViewHolder(recyclerView, 0);

    // then
    Assertions.assertThat(addProductsToBulkEntriesViewHolder).isNotNull();
  }

  @Test
  public void testOnBindViewHolder() {
    // given
    final AddProductsToBulkEntriesViewHolder mockHolder = Mockito.mock(AddProductsToBulkEntriesViewHolder.class);
    final ArrayList<ProductsToBulkEntriesViewModel> viewModels = new ArrayList<>();
    viewModel = new ProductsToBulkEntriesViewModel(product);
    viewModels.add(viewModel);
    final AddProductsToBulkEntriesAdapter adapter = new AddProductsToBulkEntriesAdapter(viewModels);
    recyclerView.setAdapter(adapter);
    adapter.filter("");

    // when
    adapter.onBindViewHolder(mockHolder, 0);

    // then
    verify(mockHolder, Mockito.times(1)).populate(viewModel, "");
  }

}