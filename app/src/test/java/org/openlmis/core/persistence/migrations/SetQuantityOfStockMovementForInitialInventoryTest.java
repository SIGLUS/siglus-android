package org.openlmis.core.persistence.migrations;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class SetQuantityOfStockMovementForInitialInventoryTest extends LMISRepositoryUnitTest {
    private SetQuantityOfStockMovementForInitialInventory migrate;
    private StockRepository stockRepository;


    @Before
    public void setUp() throws LMISException {
        migrate = new SetQuantityOfStockMovementForInitialInventory();
        migrate.setSQLiteDatabase(LmisSqliteOpenHelper.getInstance(RuntimeEnvironment.application).getWritableDatabase());

        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
    }

    @Ignore
    @Test
    public void shouldSetQuantityAsStockOnHandForInitInventory() throws Exception {
        StockMovementItem stockMovementItem = new StockMovementItem();
        StockCard stockCard = new StockCard();
        for(int i = 1; i < 10; i++) {
            stockCard.setId(i);
            stockRepository.initStockCard(stockCard);

            stockMovementItem.setStockCard(stockCard);
            stockMovementItem.setMovementDate(DateUtil.today());

            stockMovementItem.setMovementQuantity(0L);
            stockMovementItem.setStockOnHand(111L);
            stockMovementItem.setId((i - 1) * 3 + 1);
            stockRepository.saveStockItem(stockMovementItem);

            stockMovementItem.setMovementQuantity(111);
            stockMovementItem.setStockOnHand(222L);
            stockMovementItem.setId((i - 1) * 3 + 2);
            stockRepository.saveStockItem(stockMovementItem);

            stockMovementItem.setMovementQuantity(111L);
            stockMovementItem.setStockOnHand(333L);
            stockMovementItem.setId((i - 1) * 3 + 3);
            stockRepository.saveStockItem(stockMovementItem);
        }

        for(int index = 1; index < 10; index++) {
            assertThat(stockRepository.listLastFive(index).get(0).getMovementQuantity(), is(0L));
        }

        migrate.up();

        for(int index = 1; index < 10; index++) {
            assertThat(stockRepository.listLastFive(index).get(0).getMovementQuantity(), is(111L));
        }
    }
}