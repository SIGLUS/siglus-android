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

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.View;

import java.util.ArrayList;
import java.util.List;

public class StockCardListPresenter implements Presenter{

    @Inject
    StockRepository stockRepository;

    @Inject
    RnrFormItemRepository rnrFormItemRepository;

    StockCardListView view;

    public static final int STOCK_ON_HAND_NORMAL = 1;
    public static final int STOCK_ON_HAND_LOW_STOCK = 2;
    public static final int STOCK_ON_HAND_STOCK_OUT = 3;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }


    public List<StockCard> loadStockCards(){
        try {
            return  stockRepository.list();
        }catch (LMISException e){
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public void attachView(View v) {
        view = (StockCardListView) v;
    }

    public interface StockCardListView extends View{
    }

    public int getStockOnHandLevel(StockCard stockCard) {
        int lowStockAvg = getLowStockAvg(stockCard);
        int stockOnHand = stockCard.getStockOnHand();
        if (stockOnHand > lowStockAvg) {
            return STOCK_ON_HAND_NORMAL;
        } else if (stockOnHand > 0) {
            return STOCK_ON_HAND_LOW_STOCK;
        } else {
            return STOCK_ON_HAND_STOCK_OUT;
        }
    }

    private int getLowStockAvg(StockCard stockCard) {
        try {
            List<RnrFormItem> rnrFormItemList = rnrFormItemRepository.queryListForLowStockByProductId(stockCard.getProduct());
            long total = 0;
            for (RnrFormItem item : rnrFormItemList) {
                total += item.getIssued();
            }
            return (int) Math.ceil((total / rnrFormItemList.size()) * 0.05);
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
