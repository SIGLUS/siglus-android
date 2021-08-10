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

import static org.openlmis.core.utils.Constants.PTV_PROGRAM_CODE;

import android.content.Context;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.helper.FormHelper;
import org.openlmis.core.utils.Constants;

public class PTVRepository extends RnrFormRepository {

  @Inject
  ProductRepository productRepository;

  @Inject
  ServiceFormRepository serviceFormRepository;

  @Inject
  FormHelper formHelper;

  @Inject
  public PTVRepository(Context context) {
    super(context);
    programCode = PTV_PROGRAM_CODE;
  }

  @Override
  public List<RnrFormItem> generateRnrFormItems(RnRForm form, List<StockCard> stockCards) throws LMISException {
    List<RnrFormItem> rnrFormItems = super.generateRnrFormItems(form, stockCards);
    return fillAllPTVProduct(form, rnrFormItems);
  }

  @Override
  protected List<RegimenItem> generateRegimeItems(RnRForm form) throws LMISException {
    List<RegimenItem> regimenItems = new ArrayList<>();
    List<String> regimenCodes = Arrays.asList(Constants.PTV_REGIME_ADULT, Constants.PTV_REGIME_CHILD);
    for (String regimenCode : regimenCodes) {
      RegimenItem newRegimenItem = new RegimenItem();
      Regimen regimen = regimenRepository.getByCode(regimenCode);
      newRegimenItem.setRegimen(regimen);
      newRegimenItem.setForm(form);
      regimenItems.add(newRegimenItem);
    }
    return regimenItems;
  }

  @Override
  protected RnrFormItem createRnrFormItemByPeriod(StockCard stockCard, Date startDate, Date endDate)
      throws LMISException {
    RnrFormItem rnrFormItem = new RnrFormItem();
    List<StockMovementItem> stockMovementItems = stockMovementRepository
        .queryStockItemsByCreatedDate(stockCard.getId(), startDate, endDate);

    if (stockMovementItems.isEmpty()) {
      rnrFormItem.setReceived(0);
      rnrFormItem.setIssued((long) 0);
    } else {
      FormHelper.StockMovementModifiedItem modifiedItem = formHelper
          .assignTotalValues(stockMovementItems);
      rnrFormItem.setReceived(modifiedItem.getTotalReceived());
      rnrFormItem.setIssued(modifiedItem.getTotalIssued());
    }
    rnrFormItem.setProduct(stockCard.getProduct());
    return rnrFormItem;
  }

  protected ArrayList<RnrFormItem> fillAllPTVProduct(RnRForm form, List<RnrFormItem> rnrFormItems)
      throws LMISException {
    List<Long> productIds = productProgramRepository
        .queryActiveProductIdsByProgramWithKits(PTV_PROGRAM_CODE, false);
    List<Product> products = productRepository.queryProductsByProductIds(productIds);
    Program program = programRepository.queryByCode(PTV_PROGRAM_CODE);
    List<Service> services = serviceFormRepository.listAllActiveWithProgram(program);
    ArrayList<RnrFormItem> result = new ArrayList<>();

    for (Product product : products) {
      RnrFormItem rnrFormItem = new RnrFormItem();
      rnrFormItem.setForm(form);
      rnrFormItem.setProduct(product);
      RnrFormItem stockFormItem = getStockCardRnr(product, rnrFormItems);
      if (stockFormItem != null) {
        rnrFormItem = stockFormItem;
      } else {
        rnrFormItem.setReceived(0);
        rnrFormItem.setIssued((long) 0);
      }
      Long lastInventory = getLastRnrInventory(product);
      rnrFormItem.setIsCustomAmount(lastInventory == null);
      rnrFormItem.setInitialAmount(lastInventory);
      rnrFormItem.setRequestAmount((long) 0);
      rnrFormItem.setApprovedAmount((long) 0);
      rnrFormItem.setServiceItemListWrapper(getServiceItem(rnrFormItem, services));
      result.add(rnrFormItem);
    }
    return result;
  }


  private Long getLastRnrInventory(Product product) throws LMISException {
    List<RnRForm> rnRForms = listInclude(RnRForm.Emergency.NO, programCode);
    if (rnRForms.isEmpty() || rnRForms.size() == 1) {
      return null;
    }
    List<RnrFormItem> rnrFormItemListWrapper = rnRForms.get(rnRForms.size() - 2)
        .getRnrFormItemListWrapper();
    for (RnrFormItem item : rnrFormItemListWrapper) {
      if (item.getProduct().getId() == product.getId()) {
        return item.getInventory();
      }
    }
    return null;
  }

  private List<ServiceItem> getServiceItem(RnrFormItem rnrFormItem, List<Service> services) {
    List<ServiceItem> serviceItems = new ArrayList<>();
    for (Service service : services) {
      ServiceItem serviceItem = new ServiceItem();
      serviceItem.setService(service);
      serviceItem.setFormItem(rnrFormItem);
      serviceItems.add(serviceItem);
    }
    return serviceItems;
  }

  private RnrFormItem getStockCardRnr(Product product, List<RnrFormItem> rnrStockFormItems) {
    for (RnrFormItem item : rnrStockFormItems) {
      if (item.getProduct().getId() == product.getId()) {
        return item;
      }
    }
    return null;
  }
}
