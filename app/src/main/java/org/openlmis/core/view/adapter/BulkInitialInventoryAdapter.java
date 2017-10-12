package org.openlmis.core.view.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryHeaderViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class BulkInitialInventoryAdapter extends InventoryListAdapter<BaseViewHolder> {

    public static final int DEFAULT_PRODUCT_ID = 0;
    public static int ITEM_BASIC_HEADER = 0;
    public static int ITEM_LIST = 1;
    public static int ITEM_NON_BASIC_HEADER = 2;
    public final int FIRST_ELEMENT_POSITION_OF_THE_LIST = 0;

    public BulkInitialInventoryAdapter(List<InventoryViewModel> data) {
        super(data);
    }

    @Override
    public int getItemViewType(int position) {
        return filteredList.get(position).getViewType();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_LIST) {
            return new BulkInitialInventoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory, parent, false));
        } else if (viewType == ITEM_BASIC_HEADER) {
            return new BulkInitialInventoryHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory_header, parent, false), R.string.title_basic_products);
        } else {
            return new BulkInitialInventoryHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_initial_inventory_header, parent, false), R.string.title_non_basic_products);
        }
    }

    @Override
    public int validateAll() {
        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).isChecked() && !data.get(i).isDummyModel()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof BulkInitialInventoryViewHolder) {
            final InventoryViewModel viewModel = filteredList.get(position);
            ((BulkInitialInventoryViewHolder) holder).populate(viewModel, queryKeyWord);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void arrangeViewModels(final String keyword) {
        List<InventoryViewModel> filteredViewModels = filterViewModels(keyword);
        removeHeaders(filteredViewModels);
        addHeaders(filteredViewModels, checkIfNonBasicProductsExists(filteredViewModels));
        filteredList.clear();
        filteredList.addAll(filteredViewModels);
        this.notifyDataSetChanged();
    }

    private boolean checkIfNonBasicProductsExists(List<InventoryViewModel> filteredViewModels) {
        for (InventoryViewModel model : filteredViewModels) {
            if (!model.isBasic()) {
                return true;
            }
        }
        return false;
    }

    private int getNonBasicProductsHeaderPosition(List<InventoryViewModel> filteredViewModels) {
        int nonBasicProductsHeaderPosition = 0;
        while (filteredViewModels.size() > nonBasicProductsHeaderPosition && (filteredViewModels.get(nonBasicProductsHeaderPosition)).isBasic()) {
            nonBasicProductsHeaderPosition++;
        }
        return nonBasicProductsHeaderPosition;
    }

    private List<InventoryViewModel> filterViewModels(final String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return data;
        } else {
            List<InventoryViewModel> filteredResult = from(data).filter(new Predicate<InventoryViewModel>() {
                @Override
                public boolean apply(InventoryViewModel inventoryViewModel) {
                    return inventoryViewModel.getProduct().getProductFullName().toLowerCase().contains(keyword.toLowerCase());
                }
            }).toList();
            return new ArrayList<>(filteredResult);
        }
    }

    private void addHeaders(List<InventoryViewModel> filteredViewModels, boolean areThereNonBasicProducts) {
        if (areThereNonBasicProducts) {
            int nonBasicProductsHeaderPosition = getNonBasicProductsHeaderPosition(filteredViewModels);
            if (nonBasicProductsHeaderPosition > FIRST_ELEMENT_POSITION_OF_THE_LIST) {
                addHeaderForBasicProducts(filteredViewModels);
                nonBasicProductsHeaderPosition++;
            }
            addHeaderForNonBasicProducts(filteredViewModels, nonBasicProductsHeaderPosition);
            this.notifyItemChanged(nonBasicProductsHeaderPosition);
        } else {
            addHeaderForBasicProducts(filteredViewModels);
        }
    }

    private void removeHeaders(List<InventoryViewModel> filteredViewModels) {
        for (int i = 0; i < filteredViewModels.size(); i++) {
            if (filteredViewModels.get(i).getProductId() == DEFAULT_PRODUCT_ID) {
                filteredViewModels.remove(i);
            }
        }
    }

    private void addHeaderForNonBasicProducts(List<InventoryViewModel> nonBasicProductsModels, int position) {
        InventoryViewModel headerInventoryModel = new InventoryViewModel(Product.dummyProduct());
        headerInventoryModel.setDummyModel(true);
        headerInventoryModel.setViewType(BulkInitialInventoryAdapter.ITEM_NON_BASIC_HEADER);
        nonBasicProductsModels.add(position, headerInventoryModel);
    }

    private void addHeaderForBasicProducts(List<InventoryViewModel> inventoryViewModels) {
        Product basicProductHeader = Product.dummyProduct();
        basicProductHeader.setBasic(true);
        InventoryViewModel inventoryModelBasicHeader = new InventoryViewModel(basicProductHeader);
        inventoryModelBasicHeader.setDummyModel(true);
        inventoryModelBasicHeader.setViewType(BulkInitialInventoryAdapter.ITEM_BASIC_HEADER);
        inventoryViewModels.add(FIRST_ELEMENT_POSITION_OF_THE_LIST, inventoryModelBasicHeader);
    }
}
