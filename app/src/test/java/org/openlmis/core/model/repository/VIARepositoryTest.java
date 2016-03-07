package org.openlmis.core.model.repository;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class VIARepositoryTest {

    private StockRepository mockStockRepository;
    private ProductRepository productRepository;
    private VIARepository viaRepository;
    private Program viaProgram;

    @Before
    public void setup() throws LMISException {

        mockStockRepository = mock(StockRepository.class);
        productRepository = mock(ProductRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        viaRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(VIARepository.class);

        viaProgram = new Program("VIA", "VIA", null);
        viaProgram.setId(1l);
    }

    @Test
    public void shouldGenerateRnrItemsForKitWhenInitForm() throws LMISException {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_kit, true);

        RnRForm form = new RnRForm();
        form.setProgram(viaProgram);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(false);
        StockCard stockCard = new StockCard();
        when(mockStockRepository.listActiveStockCardsByProgramId(viaProgram.getId())).thenReturn(newArrayList(stockCard));
        Product product1 = new ProductBuilder().setIsKit(true).build();
        Product product2 = new ProductBuilder().setIsKit(true).build();
        when(productRepository.listActiveProducts(IsKit.Yes)).thenReturn(newArrayList(product1, product2));
        when(mockStockRepository.queryLastStockMovementItemBeforeDate(any(StockCard.class), any(Date.class))).thenReturn(new StockMovementItem());

        List<RnrFormItem> rnrFormItemList = viaRepository.generateRnrFormItems(form);
        assertThat(rnrFormItemList.size(), is(3));
        assertThat(rnrFormItemList.get(2).getReceived(), is(Long.MIN_VALUE));
        assertThat(rnrFormItemList.get(2).getIssued(), is(Long.MIN_VALUE));
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(ProductRepository.class).toInstance(productRepository);
        }
    }
}