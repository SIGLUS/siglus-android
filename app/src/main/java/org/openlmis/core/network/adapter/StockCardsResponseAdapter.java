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

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.Instant;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
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
import org.openlmis.core.network.model.LotMovementItemResponse;
import org.openlmis.core.network.model.LotOnHandResponse;
import org.openlmis.core.network.model.LotResponse;
import org.openlmis.core.network.model.ProductMovementResponse;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.StockCardsRemoteResponse;
import org.openlmis.core.network.model.StockMovementItemResponse;
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
    final StockCardsRemoteResponse stockcardNetworkResponse = gson.fromJson(json, StockCardsRemoteResponse.class);
    final StockCardsLocalResponse localResponse = new StockCardsLocalResponse();
    final ArrayList<StockCard> stockCards = new ArrayList<>();
    for (ProductMovementResponse productMovementModel : stockcardNetworkResponse.getProductMovements()) {
      try {
        stockCards.add(fitForStockCard(productMovementModel));
      } catch (LMISException e) {
        new LMISException(e, "StockCardsResponseAdapter.deserialize").reportToFabric();
        throw new JsonParseException("stock cards deserialize fail", e);
      }
    }
    localResponse.setStockCards(stockCards);
    return localResponse;
  }

  String mapToLocalReason(String networkType, String networkReason) throws LMISException {
    final NetworkMovementType convertedType = NetworkMovementType.convertValue(networkType);
    if (convertedType == NetworkMovementType.UNPACK_KIT) {
      return MovementReasonManager.UNPACK_KIT;
    }
    return networkReason;
  }

  private StockCard fitForStockCard(ProductMovementResponse productMovement) throws LMISException {
    final StockCard newStockCard = new StockCard();
    final Product product = productRepository.getByCode(productMovement.getProductCode());
    buildProductAndSoh(productMovement, newStockCard, product);
    buildStockMovementItemsAndLotMovementItems(newStockCard, productMovement);
    buildLotOnHandList(newStockCard, productMovement);
    return newStockCard;
  }

  private void buildProductAndSoh(ProductMovementResponse productMovement, StockCard newStockCard, Product product)
      throws LMISException {
    newStockCard.setProduct(product);
    newStockCard.setStockOnHand(productMovement.getStockOnHand());
    StockCard stockCardInDB = stockRepository.queryStockCardByProductId(product.getId());
    if (stockCardInDB != null) {
      newStockCard.setId(stockCardInDB.getId());
    }
  }

  private void buildStockMovementItemsAndLotMovementItems(StockCard stockCard, ProductMovementResponse productMovement)
      throws LMISException {
    final List<StockMovementItemResponse> stockMovementItemsResponse = productMovement.getStockMovementItems();
    if (CollectionUtils.isEmpty(stockMovementItemsResponse)) {
      return;
    }
    final List<StockMovementItem> stockMovementItemsWrapper = stockCard.getStockMovementItemsWrapper();
    for (StockMovementItemResponse movementItemResponse : stockMovementItemsResponse) {
      final StockMovementItem stockMovementItem = buildStockMovementItem(stockCard, movementItemResponse);
      if (CollectionUtils.isEmpty(movementItemResponse.getLotMovementItems())) {
        stockMovementItem.setReason(mapToLocalReason(movementItemResponse.getType(), movementItemResponse.getReason()));
        stockMovementItem.setDocumentNumber(movementItemResponse.getDocumentNumber());
      } else {
        final List<LotMovementItem> lotMovementItemListWrapper = stockMovementItem.getLotMovementItemListWrapper();
        for (LotMovementItemResponse lotMovementItemResponse : movementItemResponse.getLotMovementItems()) {
          final LotMovementItem lotMovementItem = buildLotMovementItem(stockCard, movementItemResponse,
              stockMovementItem, lotMovementItemResponse);
          lotMovementItemListWrapper.add(lotMovementItem);
        }
      }
      stockMovementItemsWrapper.add(stockMovementItem);
    }
  }

  @NonNull
  private StockMovementItem buildStockMovementItem(StockCard stockCard, StockMovementItemResponse movementItemResponse)
      throws LMISException {
    final StockMovementItem stockMovementItem = new StockMovementItem();
    // set movement item property
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.setSynced(true);
    stockMovementItem.setMovementQuantity(Math.abs(movementItemResponse.getMovementQuantity()));
    stockMovementItem.setStockOnHand(Long.parseLong(movementItemResponse.getStockOnHand()));
    stockMovementItem.setSignature(movementItemResponse.getSignature());
    String processedDate = movementItemResponse.getProcessedDate();
    String serverProcessedDate = movementItemResponse.getServerProcessedDate();
    String createdTime = (processedDate == null || processedDate.isEmpty())
            ? serverProcessedDate : processedDate;
    stockMovementItem.setCreatedTime(Instant.parse(createdTime).toDate());
    stockMovementItem.setRequested(movementItemResponse.getRequested());
    stockMovementItem
        .setMovementDate(DateUtil.parseString(movementItemResponse.getOccurredDate(), DateUtil.DB_DATE_FORMAT));
    final MovementType stockCardMovementType = NetworkMovementType
        .mapToLocalMovementType(movementItemResponse.getType(), movementItemResponse.getMovementQuantity());
    stockMovementItem.setMovementType(stockCardMovementType);
    return stockMovementItem;
  }

  @NonNull
  private LotMovementItem buildLotMovementItem(StockCard stockCard, StockMovementItemResponse movementItemResponse,
      StockMovementItem stockMovementItem, LotMovementItemResponse lotMovementItemResponse) throws LMISException {
    final LotMovementItem lotMovementItem = new LotMovementItem();
    final Lot lot = new Lot();
    lot.setProduct(stockCard.getProduct());
    lot.setLotNumber(lotMovementItemResponse.getLotCode());
    lotMovementItem.setLot(lot);
    lotMovementItem.setStockMovementItem(stockMovementItem);
    lotMovementItem.setMovementQuantity((long) lotMovementItemResponse.getQuantity());
    final String reason = mapToLocalReason(movementItemResponse.getType(), lotMovementItemResponse.getReason());
    // TODO stockMovement should not set reason and detail page should not use stockMovement reason
    stockMovementItem.setReason(reason);
    lotMovementItem.setReason(reason);
    lotMovementItem.setStockOnHand((long) lotMovementItemResponse.getStockOnHand());
    lotMovementItem.setDocumentNumber(lotMovementItemResponse.getDocumentNumber());
    return lotMovementItem;
  }

  private void buildLotOnHandList(StockCard stockCard, ProductMovementResponse productMovement) throws LMISException {
    final List<LotOnHandResponse> lotsOnHandsResponse = productMovement.getLotsOnHand();
    if (CollectionUtils.isEmpty(lotsOnHandsResponse)) {
      return;
    }
    final List<LotOnHand> lotOnHandListWrapper = stockCard.getLotOnHandListWrapper();
    for (LotOnHandResponse lotOnHandItemResponse : lotsOnHandsResponse) {
      if (lotOnHandItemResponse.getLot() == null) {
        continue;
      }
      final LotResponse lotResponse = lotOnHandItemResponse.getLot();
      // set lot info
      Lot lot = lotRepository
          .getLotByLotNumberAndProductId(lotResponse.getLotCode(), stockCard.getProduct().getId());
      if (lot == null) {
        lot = new Lot();
        lot.setLotNumber(lotResponse.getLotCode());
        lot.setProduct(stockCard.getProduct());
      }
      lot.setExpirationDate(DateUtil.parseString(lotResponse.getExpirationDate(), DateUtil.DB_DATE_FORMAT));
      // set lot on hand
      final LotOnHand lotOnHand = new LotOnHand();
      lotOnHand.setLot(lot);
      lotOnHand.setQuantityOnHand((long) lotOnHandItemResponse.getQuantityOnHand());
      lotOnHand.setStockCard(stockCard);
      lotOnHandListWrapper.add(lotOnHand);
    }
  }

  public enum NetworkMovementType {
    PHYSICAL_INVENTORY() {
      @Override
      public MovementType toMovementType(int movementQuantity) {
        if (movementQuantity == 0) {
          return MovementType.PHYSICAL_INVENTORY;
        } else if (movementQuantity > 0) {
          return MovementType.POSITIVE_ADJUST;
        } else {
          return MovementType.NEGATIVE_ADJUST;
        }
      }
    },
    RECEIVE() {
      @Override
      public MovementType toMovementType(int movementQuantity) {
        return MovementType.RECEIVE;
      }
    },
    ISSUE() {
      @Override
      public MovementType toMovementType(int movementQuantity) {
        return MovementType.ISSUE;
      }
    },
    ADJUSTMENT() {
      @Override
      public MovementType toMovementType(int movementQuantity) {
        if (movementQuantity > 0) {
          return MovementType.POSITIVE_ADJUST;
        } else if (movementQuantity < 0) {
          return MovementType.NEGATIVE_ADJUST;
        } else {
          throw new IllegalArgumentException("Adjustment quantity cannot be 0");
        }
      }
    },
    UNPACK_KIT() {
      @Override
      public MovementType toMovementType(int movementQuantity) {
        return MovementType.ISSUE;
      }
    };

    public MovementType toMovementType(int movementQuantity) {
      throw new UnsupportedOperationException("Please override this method for " + this.name());
    }

    public static NetworkMovementType convertValue(String type) throws LMISException {
      for (NetworkMovementType movementType : NetworkMovementType.values()) {
        if (type.equalsIgnoreCase(movementType.name())) {
          return movementType;
        }
      }
      throw new LMISException("Illegal network movement type: " + type);
    }

    public static MovementType mapToLocalMovementType(String networkType, int movementQuantity) throws LMISException {
      return convertValue(networkType).toMovementType(movementQuantity);
    }
  }
}
