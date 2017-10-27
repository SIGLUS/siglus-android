package org.openlmis.core.builders;

import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PTVProgramStockInformationBuilder {

    public static final long DEFAULT_QUANTITY = 0L;

    @Inject
    ProductRepository productRepository;

    @Inject
    ServiceDispensationBuilder serviceDispensationBuilder;

    @Inject
    StockRepository stockRepository;

    public List<PTVProgramStockInformation> buildPTVProgramStockInformation(PTVProgram ptvProgram) throws LMISException {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>();
        List<Product> products = productRepository.getProductsByCodes(getProductCodes());
        for (Product product : products) {
            PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
            ptvProgramStockInformation.setProduct(product);
            ptvProgramStockInformation.setPtvProgram(ptvProgram);
            List<ServiceDispensation> serviceDispensations = serviceDispensationBuilder.buildInitialServiceDispensations(ptvProgramStockInformation);
            ptvProgramStockInformation.setServiceDispensations(serviceDispensations);
            setStockCardInformation(product, ptvProgramStockInformation);
            ptvProgramStocksInformation.add(ptvProgramStockInformation);
        }
        return ptvProgramStocksInformation;
    }

    private void setStockCardInformation(Product product, PTVProgramStockInformation ptvProgramStockInformation) throws LMISException {
        StockCard stockCard = stockRepository.queryStockCardByProductCode(product.getCode());
        if (stockCard != null) {
            ptvProgramStockInformation.setInitialStock(stockCard.getStockOnHand());
            List<StockMovementItem> stockMovementItems = ((List<StockMovementItem>) stockCard.getForeignStockMovementItems());
            ptvProgramStockInformation.setEntries(getEntriesQuantity(stockMovementItems));
        } else {
            ptvProgramStockInformation.setInitialStock(DEFAULT_QUANTITY);
            ptvProgramStockInformation.setEntries(DEFAULT_QUANTITY);
        }
    }

    private long getEntriesQuantity(List<StockMovementItem> stockMovementItems) {
        if (stockMovementItems != null) {
            for (StockMovementItem movementItem : stockMovementItems) {
                if (movementItem.getMovementType().equals(MovementReasonManager.MovementType.RECEIVE)) {
                    return movementItem.getMovementQuantity();
                }
            }
        }
        return DEFAULT_QUANTITY;
    }

    @NonNull
    private List<String> getProductCodes() {
        List<String> ptvProductCodes = new ArrayList<>();
        ptvProductCodes.add(Constants.PTV_PRODUCT_ONE_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_TWO_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_THREE_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_FOUR_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_FIVE_CODE);
        return ptvProductCodes;
    }
}
