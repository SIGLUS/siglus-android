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

package org.openlmis.core.view.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.LotBuilder;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.adapter.StockcardListLotAdapter.LotInfoHolder;

@RunWith(LMISTestRunner.class)
public class StockcardListLotAdapterTest {

  private static final String LotNumber = "lotNumber";
  private static final long quantity = 100L;

  private ArrayList<LotOnHand> lotOnHands;

  @Before
  public void setup() {
    final LotOnHand lotOnHand1 = generateLotOnHandByExpireDate(
        DateUtil.parseString("2021-05-22", DateUtil.DB_DATE_FORMAT));
    final LotOnHand lotOnHand2 = generateLotOnHandByExpireDate(
        DateUtil.parseString("2021-05-21", DateUtil.DB_DATE_FORMAT));
    final LotOnHand lotOnHand3 = generateLotOnHandByExpireDate(
        DateUtil.parseString("2021-05-23", DateUtil.DB_DATE_FORMAT));
    lotOnHands = new ArrayList<>();
    lotOnHands.add(lotOnHand1);
    lotOnHands.add(lotOnHand2);
    lotOnHands.add(lotOnHand3);
  }

  @Test
  public void shouldListSortByExpiryDate() {

    // when
    final StockcardListLotAdapter adapter = new StockcardListLotAdapter(lotOnHands);

    // then
    Assertions.assertThat(adapter.lotInfoList.get(0)).isEqualTo(lotOnHands.get(1));
    Assertions.assertThat(adapter.lotInfoList.get(1)).isEqualTo(lotOnHands.get(0));
    Assertions.assertThat(adapter.lotInfoList.get(2)).isEqualTo(lotOnHands.get(2));
  }

  @Test
  public void shouldDoNothingWithNullList() {
    // when
    final StockcardListLotAdapter adapter = new StockcardListLotAdapter(null);

    // then
    Assertions.assertThat(adapter.lotInfoList).isNull();
  }

  @Test
  public void shouldBindCorrectDataToHolder() {
    // given
    final LotInfoHolder lotInfoHolder = new LotInfoHolder(Mockito.mock(View.class));
    final TextView mockLotCodeTextView = Mockito.mock(TextView.class);
    final TextView mockExpiryDateTextView = Mockito.mock(TextView.class);
    final TextView mockLotOnHandTextView = Mockito.mock(TextView.class);
    lotInfoHolder.lotCode = mockLotCodeTextView;
    lotInfoHolder.expiryDate = mockExpiryDateTextView;
    lotInfoHolder.lotOnHand = mockLotOnHandTextView;
    final StockcardListLotAdapter adapter = new StockcardListLotAdapter(lotOnHands);

    // when
    adapter.onBindViewHolder(lotInfoHolder, 0);

    // then
    Mockito.verify(mockLotCodeTextView, Mockito.times(1)).setText(String.format("[%s]", LotNumber));
    Mockito.verify(mockExpiryDateTextView, Mockito.times(1)).setText("21/05/2021");
    Mockito.verify(mockLotOnHandTextView, Mockito.times(1)).setText(String.valueOf(quantity));
  }

  @Test
  public void shouldGetZeroItemCountWithNullData() {
    // given
    final StockcardListLotAdapter adapter = new StockcardListLotAdapter(null);

    // when
    final int itemCount = adapter.getItemCount();

    // then
    Assertions.assertThat(itemCount).isZero();
  }

  @Test
  public void shouldGetCorrectItemCountWithNormalData() {
    // given
    final StockcardListLotAdapter adapter = new StockcardListLotAdapter(lotOnHands);

    // when
    final int itemCount = adapter.getItemCount();

    // then
    Assertions.assertThat(itemCount).isEqualTo(3);
  }

  @Test
  public void shouldCorrectlyCreateHolder() {
    // given
    final StockcardListLotAdapter adapter = new StockcardListLotAdapter(lotOnHands);
    final LinearLayout recyclerView = new LinearLayout(LMISTestApp.getContext());

    // when
    final LotInfoHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);

    // then
    Assertions.assertThat(viewHolder.lotCode).isNotNull();
    Assertions.assertThat(viewHolder.expiryDate).isNotNull();
    Assertions.assertThat(viewHolder.lotOnHand).isNotNull();
  }

  private LotOnHand generateLotOnHandByExpireDate(Date expireDate) {
    final Lot lot = LotBuilder.create().setExpirationDate(expireDate).setLotNumber(LotNumber).build();
    return new LotOnHand(lot, new StockCard(), quantity);
  }
}