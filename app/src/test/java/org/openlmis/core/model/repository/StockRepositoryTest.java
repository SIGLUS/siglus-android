package org.openlmis.core.model.repository;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.NotNull;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import org.robolectric.Robolectric;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class StockRepositoryTest extends LMISRepositoryUnitTest {

    StockRepository stockRepository;
    ProductRepository productRepository;
    Product product;

    @Before
    public void setup() throws LMISException{

        stockRepository = RoboGuice.getInjector(Robolectric.application).getInstance(StockRepository.class);
        productRepository = RoboGuice.getInjector(Robolectric.application).getInstance(ProductRepository.class);

        product = new Product();
        product.setName("Test Product");
        product.setUnit("200");

        productRepository.create(product);

    }



    @Test
    public void shouldSaveStockCardsSuccessful() throws LMISException{

        StockCard stockCard = new StockCard();
        stockCard.setStockCardId("ID");
        stockCard.setStockOnHand(1);
        stockCard.setProduct(product);
        stockRepository.save(stockCard);


        assertThat(stockRepository.list().size(), is(1));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
    }


    @Test
    public void shouldBathSaveSuccessful() throws LMISException{

        ArrayList<StockCard> stockCards = new ArrayList<>();

        for (int i =0; i < 10;i++){
            StockCard stockCard = new StockCard();
            stockCard.setStockCardId("ID" + i);
            stockCard.setStockOnHand(i);
            stockCard.setProduct(product);

            stockCards.add(stockCard);
        }

        stockRepository.batchSave(stockCards);

        assertThat(stockRepository.list().size(), is(10));
        assertThat(stockRepository.list().get(0).getProduct(), is(NotNull.NOT_NULL));
    }

    @Test
    public void shouldGetStockItemsInPeriod() throws LMISException{
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(1000);
        stockCard.setProduct(product);

        stockRepository.save(stockCard);


        ArrayList<StockItem> stockItems = new ArrayList<>();
        for (int i= 0; i < 10; i++){
            StockItem item = new StockItem();
            item.setStockOnHand(i);
            item.setProduct(product);
            item.setStockCard(stockCard);

            item.setAmount(i);

            if (i%2 == 0) {
                item.setMovementType(StockRepository.MOVEMENTTYPE.RECEIVE.toString());
            }else {
                item.setMovementType(StockRepository.MOVEMENTTYPE.ISSUE.toString());
            }

            item.setDocumentNumber("DOC" + i);

            stockItems.add(item);
        }

        stockRepository.saveStockItems(stockItems);
        List<StockItem> retItems = stockRepository.listStockItems();

        assertThat(retItems.size(), is(10));

        int sum1 = stockRepository.sum(StockRepository.MOVEMENTTYPE.ISSUE.toString(), stockCard, new Date(), new Date());
        int sum2 = stockRepository.sum(StockRepository.MOVEMENTTYPE.RECEIVE.toString(), stockCard, new Date(), new Date());

        assertThat(sum1, is(25));
        assertThat(sum2, is(20));
    }
}
