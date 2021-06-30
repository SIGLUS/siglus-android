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

package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class StockCardsNetworkResponse {

  private List<ProductMovementResponse> productMovements;

  @NoArgsConstructor
  @Data
  public static class ProductMovementResponse {

    private int stockOnHand;
    private String productCode;
    private List<StockMovementItemResponse> stockMovementItems;
    private List<LotOnHandResponse> lotsOnHand;
  }

  @NoArgsConstructor
  @Data
  public static class StockMovementItemResponse {

    private int movementQuantity;
    private int requested;
    private long processedDate;
    private String type;
    private String stockOnHand;
    private String signature;
    private String occurredDate;
    private String documentNumber;
    private String reason;
    private List<LotMovementItemResponse> lotMovementItems;
  }

  @NoArgsConstructor
  @Data
  public static class LotMovementItemResponse {

    private int quantity;
    private int stockOnHand;
    private String documentNumber;
    private String reason;
    private String lotCode;
  }

  @NoArgsConstructor
  @Data
  public static class LotOnHandResponse {

    private int quantityOnHand;
    private String effectiveDate;
    private LotResponse lot;
  }

  @NoArgsConstructor
  @Data
  public static class LotResponse {

    private boolean valid;
    private String lotCode;
    private String expirationDate;
  }
}
