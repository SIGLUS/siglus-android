package org.openlmis.core.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.view.holder.PTVProgramViewHolder;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;
import org.openlmis.core.model.PTVProgramStockInformation;

import java.util.ArrayList;
import java.util.List;

import static org.openlmis.core.utils.Constants.ENTRIES;
import static org.openlmis.core.utils.Constants.FINAL_STOCK;
import static org.openlmis.core.utils.Constants.LOSSES_AND_ADJUSTMENTS;
import static org.openlmis.core.utils.Constants.TOTAL;

public class PTVProgramAdapter extends RecyclerView.Adapter<PTVProgramViewHolder> {

    public static final int TOTAL_SERVICES_NUMBER = 8;
    private final int NUMBER_OF_PRODUCTS = 5;
    private PTVProgram ptvProgram;

    private List<PTVViewModel> ptvViewModels;

    private RecyclerView view;
    private boolean needsRefresh;
    private int finalStockPosition;
    private static final int FIFTH_PRODUCT_POSITION = 4;
    private static final int FOURTH_PRODUCT_POSITION = 3;
    private static final int THIRD_PRODUCT_POSITION = 2;
    private static final int SECOND_PRODUCT_POSITION = 1;
    private static final int FIRST_PRODUCT_POSITION = 0;

    public PTVProgramAdapter(PTVProgram ptvProgram, List<PTVViewModel> ptvViewModels) {
        this.ptvProgram = ptvProgram;
        this.ptvViewModels = ptvViewModels;
        calculateTotal();
        calculateFinalStock();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        view = recyclerView;
    }


    private int getTotalElementPosition() {
        for (PTVViewModel model : ptvViewModels) {
            if (model.getPlaceholderItemName().equals(TOTAL)) {
                return ptvViewModels.indexOf(model);
            }
        }
        return -1;
    }

    @Override
    public PTVProgramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (ptvProgram.getStatus().equals(ViaReportStatus.SUBMITTED)) {
            return new PTVProgramViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ptv_report_form_row, parent, false), true);
        }
        return new PTVProgramViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ptv_report_form_row, parent, false), false);
    }

    @Override
    public void onBindViewHolder(PTVProgramViewHolder holder, int position) {
        holder.populate(ptvViewModels.get(position));
        holder.setWatchersForEditableListeners(productQuantitiesWatcher(holder));
    }

    private void calculateTotal() {
        long[] totals = sumValuesForEachProduct();
        assignTotalsToEachModel(totals);
    }

    private void assignTotalsToEachModel(long[] totals) {
        PTVViewModel ptvViewModel = ptvViewModels.get(getTotalElementPosition());
        ptvViewModel.setQuantity(1, totals[0]);
        ptvViewModel.setQuantity(2, totals[1]);
        ptvViewModel.setQuantity(3, totals[2]);
        ptvViewModel.setQuantity(4, totals[3]);
        ptvViewModel.setQuantity(5, totals[4]);
    }

    private long[] sumValuesForEachProduct() {
        long[] totals = new long[NUMBER_OF_PRODUCTS];
        for (int actualServicePosition = 0; actualServicePosition < TOTAL_SERVICES_NUMBER; actualServicePosition++) {
            totals[FIRST_PRODUCT_POSITION] += ptvViewModels.get(actualServicePosition).getQuantity1();
            totals[SECOND_PRODUCT_POSITION] += ptvViewModels.get(actualServicePosition).getQuantity2();
            totals[THIRD_PRODUCT_POSITION] += ptvViewModels.get(actualServicePosition).getQuantity3();
            totals[FOURTH_PRODUCT_POSITION] += ptvViewModels.get(actualServicePosition).getQuantity4();
            totals[FIFTH_PRODUCT_POSITION] += ptvViewModels.get(actualServicePosition).getQuantity5();
        }
        return totals;
    }

    //TODO It will change because of Diana's new requirements
    private void calculateFinalStock() {
        long[] entry = new long[5];
        long[] total = new long[5];
        long[] lossesAndAdjustments = new long[5];
        finalStockPosition = -1;

        for (PTVViewModel model : ptvViewModels) {
            switch (model.getPlaceholderItemName()) {
                case ENTRIES:
                    entry[0] = model.getQuantity1();
                    entry[1] = model.getQuantity2();
                    entry[2] = model.getQuantity3();
                    entry[3] = model.getQuantity4();
                    entry[4] = model.getQuantity5();
                    break;
                case TOTAL:
                    total[0] = model.getQuantity1();
                    total[1] = model.getQuantity2();
                    total[2] = model.getQuantity3();
                    total[3] = model.getQuantity4();
                    total[4] = model.getQuantity5();
                    break;
                case LOSSES_AND_ADJUSTMENTS:
                    lossesAndAdjustments[0] = model.getQuantity1();
                    lossesAndAdjustments[1] = model.getQuantity2();
                    lossesAndAdjustments[2] = model.getQuantity3();
                    lossesAndAdjustments[3] = model.getQuantity4();
                    lossesAndAdjustments[4] = model.getQuantity5();
                    break;
                case FINAL_STOCK:
                    finalStockPosition = ptvViewModels.indexOf(model);
                    break;
            }
        }
        List<PTVProgramStockInformation> stocksInformation = new ArrayList<>(ptvProgram.getPtvProgramStocksInformation());
        for (int position = 0; position < stocksInformation.size(); position++) {
            long finalStockForEachPosition = stocksInformation.get(position).getInitialStock() + entry[position] - total[position] - lossesAndAdjustments[position];
            ptvViewModels.get(finalStockPosition).setQuantity(position + 1, finalStockForEachPosition);
        }
    }

    @NonNull
    private TextWatcher productQuantitiesWatcher(final PTVProgramViewHolder holder) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                needsRefresh = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.updateProductQuantities();
                calculateTotal();
                calculateFinalStock();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!needsRefresh && !view.isComputingLayout()) {
                    notifyItemChanged(getTotalElementPosition());
                    notifyItemChanged(finalStockPosition);
                    needsRefresh = true;
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return ptvViewModels.size();
    }
}
