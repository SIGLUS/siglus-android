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

package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.StockCardsNetworkResponse;
import org.openlmis.core.network.model.StockCardsNetworkResponse.LotMovementItemResponse;
import org.openlmis.core.network.model.StockCardsNetworkResponse.LotOnHandResponse;
import org.openlmis.core.network.model.StockCardsNetworkResponse.LotResponse;
import org.openlmis.core.network.model.StockCardsNetworkResponse.ProductMovementResponse;
import org.openlmis.core.network.model.StockCardsNetworkResponse.StockMovementItemResponse;
import org.openlmis.core.utils.DateUtil;
import roboguice.RoboGuice;

public class StockCardsResponseAdapter implements JsonDeserializer<StockCardsLocalResponse> {

  private final Gson gson = new Gson();
  @Inject
  private ProductRepository productRepository;

  @Inject
  private StockRepository stockRepository;

  @Inject
  private LotRepository lotRepository;

  @Inject
  public StockCardsResponseAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
  }

  @Override
  public StockCardsLocalResponse deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    final StockCardsNetworkResponse stockcardNetworkResponse = gson.fromJson(json, StockCardsNetworkResponse.class);
    final StockCardsLocalResponse adaptedResponse = new StockCardsLocalResponse();
    final ArrayList<StockCard> stockCards = new ArrayList<>();
    for (ProductMovementResponse productMovementModel : stockcardNetworkResponse.getProductMovements()) {
      try {
        stockCards.add(fitForStockCard(productMovementModel));
      } catch (LMISException e) {
        new LMISException(e, "StockCardsResponseAdapter.deserialize").reportToFabric();
        throw new JsonParseException("stock cards deserialize fail", e);
      }
    }
    adaptedResponse.setStockCards(stockCards);
    return adaptedResponse;
  }

  MovementType mapToLocalMovementType(String networkType, int movementQuantity) throws LMISException {
    final NetworkMovementType convertedType = NetworkMovementType.convertValue(networkType);
    if (convertedType == NetworkMovementType.PHYSICAL_INVENTORY && movementQuantity == 0) {
      return MovementType.PHYSICAL_INVENTORY;
    } else if (convertedType == NetworkMovementType.PHYSICAL_INVENTORY && movementQuantity > 0) {
      return MovementType.POSITIVE_ADJUST;
    } else if (convertedType == NetworkMovementType.PHYSICAL_INVENTORY) {
      return MovementType.NEGATIVE_ADJUST;
    } else if (convertedType == NetworkMovementType.RECEIVE) {
      return MovementType.RECEIVE;
    } else if (convertedType == NetworkMovementType.ISSUE || convertedType == NetworkMovementType.UNPACK_KIT) {
      return MovementType.ISSUE;
    } else if (convertedType == NetworkMovementType.ADJUSTMENT && movementQuantity > 0) {
      return MovementType.POSITIVE_ADJUST;
    } else if (convertedType == NetworkMovementType.ADJUSTMENT && movementQuantity < 0) {
      return MovementType.NEGATIVE_ADJUST;
    } else {
      throw new LMISException(
          String.format("Illegal arguments: networkType = %s, movementQuantity = %s", networkType, movementQuantity));
    }
  }

  private StockCard fitForStockCard(ProductMovementResponse productMovement) throws LMISException {
    final StockCard newStockCard = new StockCard();
    // setup stock card
    final Product product = productRepository.getByCode(productMovement.getProductCode());
    setupStockCard(productMovement, newStockCard, product);
    setStockMovementItemsAndLotMovementItems(newStockCard, product, productMovement);
    setupLotOnHandList(newStockCard, productMovement);
    return newStockCard;
  }

  private void setupStockCard(ProductMovementResponse productMovement, StockCard newStockCard, Product product)
      throws LMISException {
    newStockCard.setProduct(product);
    newStockCard.setStockOnHand(productMovement.getStockOnHand());
    StockCard stockCardInDB = stockRepository.queryStockCardByProductId(product.getId());
    if (stockCardInDB != null) {
      newStockCard.setId(stockCardInDB.getId());
    }
  }

  private void setStockMovementItemsAndLotMovementItems(StockCard stockCard, Product product,
      ProductMovementResponse productMovement) throws LMISException {
    final List<StockMovementItemResponse> stockMovementItemsResponse = productMovement.getStockMovementItems();
    if (CollectionUtils.isEmpty(stockMovementItemsResponse)) {
      return;
    }
    final List<StockMovementItem> stockMovementItemsWrapper = stockCard.getStockMovementItemsWrapper();
    for (StockMovementItemResponse stockMovementItemResponse : stockMovementItemsResponse) {
      final StockMovementItem stockMovementItem = new StockMovementItem();
      // set movement item property
      stockMovementItem.setStockCard(stockCard);
      stockMovementItem.setSynced(true);
      stockMovementItem.setMovementQuantity(stockMovementItemResponse.getMovementQuantity());
      stockMovementItem.setStockOnHand(Long.parseLong(stockMovementItemResponse.getStockOnHand()));
      stockMovementItem.setSignature(stockMovementItemResponse.getSignature());
      stockMovementItem.setCreatedTime(new Date(stockMovementItemResponse.getProcessedDate()));
      stockMovementItem.setRequested((long) stockMovementItemResponse.getRequested());
      stockMovementItem
          .setMovementDate(DateUtil.parseString(stockMovementItemResponse.getOccurredDate(), DateUtil.DB_DATE_FORMAT));
      final MovementType stockCardMovementType = mapToLocalMovementType(stockMovementItemResponse.getType(),
          stockMovementItemResponse.getMovementQuantity());
      stockMovementItem.setMovementType(stockCardMovementType);
      if (CollectionUtils.isEmpty(stockMovementItemResponse.getLotMovementItems())) {
        stockMovementItem.setReason(stockMovementItemResponse.getReason());
        stockMovementItem.setDocumentNumber(stockMovementItemResponse.getDocumentNumber());
      } else {
        final List<LotMovementItem> lotMovementItemListWrapper = stockMovementItem.getLotMovementItemListWrapper();
        for (LotMovementItemResponse lotMovementItemResponse : stockMovementItemResponse.getLotMovementItems()) {
          final LotMovementItem lotMovementItem = new LotMovementItem();
          final Lot lot = new Lot();
          lot.setProduct(product);
          lot.setLotNumber(lotMovementItemResponse.getLotCode());
          lotMovementItem.setLot(lot);
          lotMovementItem.setMovementQuantity((long) lotMovementItemResponse.getQuantity());
          lotMovementItem.setReason(lotMovementItemResponse.getReason());
          lotMovementItem.setStockOnHand((long) lotMovementItemResponse.getStockOnHand());
          lotMovementItem.setDocumentNumber(lotMovementItemResponse.getDocumentNumber());
          lotMovementItemListWrapper.add(lotMovementItem);
        }
      }
      stockMovementItemsWrapper.add(stockMovementItem);
    }
  }

  private void setupLotOnHandList(StockCard stockCard, ProductMovementResponse productMovement) throws LMISException {
    final List<LotOnHandResponse> lotsOnHandsResponse = productMovement.getLotsOnHand();
    if (CollectionUtils.isEmpty(lotsOnHandsResponse)) {
      return;
    }
    final List<LotOnHand> lotOnHandListWrapper = stockCard.getLotOnHandListWrapper();
    for (LotOnHandResponse lotOnHandItemResponse : lotsOnHandsResponse) {
      final LotResponse lotResponse = lotOnHandItemResponse.getLot();
      // set lot
      final Lot lot = new Lot();
      lot.setLotNumber(lotResponse.getLotCode());
      lot.setExpirationDate(DateUtil.parseString(lotResponse.getExpirationDate(), DateUtil.DB_DATE_FORMAT));
      lot.setProduct(stockCard.getProduct());
      // set lot on hand
      final LotOnHand lotOnHand = new LotOnHand();
      lotOnHand.setLot(lot);
      lotOnHand.setQuantityOnHand((long) lotOnHandItemResponse.getQuantityOnHand());
      lotOnHand.setStockCard(stockCard);
      lotOnHandListWrapper.add(lotOnHand);
      // set exist lot on hand info
      updateLotOnHandIdAndLotIfLotAlreadyExist(lotOnHand);
    }
  }

  private void updateLotOnHandIdAndLotIfLotAlreadyExist(LotOnHand lotOnHand) throws LMISException {
    Product product = lotOnHand.getLot().getProduct();
    Lot existingLot = lotRepository
        .getLotByLotNumberAndProductId(lotOnHand.getLot().getLotNumber(), product.getId());
    if (existingLot != null) {
      lotOnHand.setId(lotRepository.getLotOnHandByLot(existingLot).getId());
      lotOnHand.setLot(existingLot);
    }
  }

  public enum NetworkMovementType {
    PHYSICAL_INVENTORY, RECEIVE, ISSUE, ADJUSTMENT, UNPACK_KIT;

    public static NetworkMovementType convertValue(String type) throws LMISException {
      for (NetworkMovementType movementType : NetworkMovementType.values()) {
        if (type.equalsIgnoreCase(movementType.name())) {
          return movementType;
        }
      }
      throw new LMISException("Illegal network movement type: " + type);
    }
  }
}
