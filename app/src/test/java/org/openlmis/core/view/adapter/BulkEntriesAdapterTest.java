package org.openlmis.core.view.adapter;

import static org.junit.Assert.assertEquals;
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
import org.openlmis.core.view.holder.BulkEntriesViewHolder;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel;

@RunWith(LMISTestRunner.class)
public class BulkEntriesAdapterTest {

  private RecyclerView recyclerView;
  private BulkEntriesViewModel viewModel;
  private Product product;

  @Before
  public void setUp() throws Exception {
    recyclerView = new RecyclerView(LMISTestApp.getContext());
    recyclerView.setLayoutManager(new LinearLayoutManager(LMISTestApp.getContext()));
    product = Product.builder()
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
    final ArrayList<BulkEntriesViewModel> viewModels = new ArrayList<>();
    final BulkEntriesAdapter adapter = new BulkEntriesAdapter(viewModels);
    recyclerView.setAdapter(adapter);

    // when
    final BulkEntriesViewHolder bulkEntriesViewHolder = adapter.onCreateViewHolder(recyclerView, 0);

    // then
    Assertions.assertThat(bulkEntriesViewHolder).isNotNull();
  }

  @Test
  public void testOnBindViewHolder() {
    // given
    final BulkEntriesViewHolder mockHolder = Mockito.mock(BulkEntriesViewHolder.class);
    final ArrayList<BulkEntriesViewModel> viewModels = new ArrayList<>();
    viewModel = new BulkEntriesViewModel(product);
    viewModels.add(viewModel);

    final BulkEntriesAdapter adapter = new BulkEntriesAdapter(viewModels);
    recyclerView.setAdapter(adapter);

    // when
    adapter.onBindViewHolder(mockHolder, 0);

    // then
    verify(mockHolder, Mockito.times(1)).populate(viewModel, adapter);
  }

  @Test
  public void shouldRemoveViewModel() {
    // given
    final ArrayList<BulkEntriesViewModel> viewModels = new ArrayList<>();
    viewModel = new BulkEntriesViewModel(product);
    viewModels.add(viewModel);
    final BulkEntriesAdapter adapter = new BulkEntriesAdapter(viewModels);
    // when
    adapter.remove(viewModel,0);
    // then
    assertEquals(0, viewModels.size());

  }

}