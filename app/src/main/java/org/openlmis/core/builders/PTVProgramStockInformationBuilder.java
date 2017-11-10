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
        StockCard stockCard = stockRepository.queryStockCardByProductId(product.getId());
        if (stockCard != null) {
            List<StockMovementItem> stockMovementItems = new ArrayList<>(stockCard.getForeignStockMovementItems());
            long entriesQuantity = getEntriesQuantity(stockMovementItems);
            long initialStock = stockCard.getStockOnHand() - entriesQuantity;
            if (initialStock < 0) {
                initialStock = DEFAULT_QUANTITY;
            }
            ptvProgramStockInformation.setInitialStock(initialStock);
            ptvProgramStockInformation.setEntries(entriesQuantity);
        } else {
            ptvProgramStockInformation.setInitialStock(DEFAULT_QUANTITY);
            ptvProgramStockInformation.setEntries(DEFAULT_QUANTITY);
        }
    }

    private long getEntriesQuantity(List<StockMovementItem> stockMovementItems) {
        long entriesQuantity = DEFAULT_QUANTITY;
        if (stockMovementItems != null) {
            for (StockMovementItem movementItem : stockMovementItems) {
                if (movementItem.getMovementType().equals(MovementReasonManager.MovementType.RECEIVE)) {
                    entriesQuantity += movementItem.getMovementQuantity();
                }
            }
        }
        return entriesQuantity;
    }

    @NonNull
    private List<String> getProductCodes() {
        List<String> ptvProductCodes = new ArrayList<>();
        ptvProductCodes.add(Constants.PTV_PRODUCT_FIRST_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_SECOND_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_THIRD_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_FOURTH_CODE);
        ptvProductCodes.add(Constants.PTV_PRODUCT_FIFTH_CODE);
        return ptvProductCodes;
    }

    public List<PTVProgramStockInformation> buildExistentPTVProgramStockInformation(PTVProgram ptvProgram) throws LMISException {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>();
        for (PTVProgramStockInformation stockInformation : ptvProgram.getPtvProgramStocksInformation()) {
            long id = stockInformation.getProduct().getId();
            Product product = productRepository.getProductById(id);
            stockInformation.setProduct(product);
            List<ServiceDispensation> serviceDispensations = serviceDispensationBuilder.buildExistentInitialServiceDispensations(stockInformation);
            stockInformation.setServiceDispensations(serviceDispensations);
            ptvProgramStocksInformation.add(stockInformation);
        }
        return ptvProgramStocksInformation;
    }
}
