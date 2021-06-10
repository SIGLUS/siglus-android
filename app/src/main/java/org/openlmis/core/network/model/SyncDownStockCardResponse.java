package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import org.openlmis.core.model.StockCard;

@Data
public class SyncDownStockCardResponse {

  List<StockCard> stockCards;
}

