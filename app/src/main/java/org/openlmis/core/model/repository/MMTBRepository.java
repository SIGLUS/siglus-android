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

package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.constant.ReportConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.MMIARepository.ReportType;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class MMTBRepository extends RnrFormRepository {

  @Inject
  public MMTBRepository(Context context) {
    super(context);
    programCode = Constants.MMTB_PROGRAM_CODE;
  }

  @Override
  public List<RnrFormItem> generateRnrFormItems(RnRForm form, List<StockCard> stockCards)
      throws LMISException {
    List<RnrFormItem> rnrFormItems = super.generateRnrFormItems(form, stockCards);
    return fillAllProducts(form, rnrFormItems);
  }

  @Override
  protected List<RegimenItemThreeLines> generateRegimeThreeLineItems(RnRForm form) {
    List<String> regimeThreeLines = new ArrayList<>();
    regimeThreeLines.add(ReportConstants.KEY_MMTB_THREE_LINE_1);
    regimeThreeLines.add(ReportConstants.KEY_MMTB_THREE_LINE_2);
    regimeThreeLines.add(ReportConstants.KEY_MMTB_THREE_LINE_3);

    return FluentIterable.from(regimeThreeLines)
        .transform(type -> {
          RegimenItemThreeLines itemThreeLines = new RegimenItemThreeLines(type);
          itemThreeLines.setForm(form);
          return itemThreeLines;
        }).toList();
  }

  @Override
  protected List<BaseInfoItem> generateBaseInfoItems(RnRForm form, ReportType type) {
    // TODO generate base info
    return super.generateBaseInfoItems(form, type);
  }

  @Override
  protected void updateInitialAmount(RnrFormItem rnrFormItem, Long lastInventory) {
    rnrFormItem.setIsCustomAmount(lastInventory == null);
    rnrFormItem.setInitialAmount(lastInventory);
  }
}
