package org.openlmis.core.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.network.LMISRestManagerMock;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@RunWith(LMISTestRunner.class)
public class SyncDownManagerIT {
    private SyncDownManager syncDownManager;
    private LMISRestManagerMock lmisRestManager;
    private ProductRepository productRepository;

    @Test
    public void shouldSyncDownLatestProductWithArchivedStatus() throws Exception {
        //given
        String json = JsonFileReader.readJson(getClass(), "SyncDownLatestProductResponse.json");
        LMISRestManagerMock.buildMockClient("/rest-api/latest-products",200,"OK",json);
        lmisRestManager = new LMISRestManagerMock();
        syncDownManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(SyncDownManager.class);
        syncDownManager.lmisRestApi = lmisRestManager.getLmisRestApi();
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_kit, true);

        //when
        syncDownManager.syncDownLatestProducts();

        //then
        Product product = productRepository.getByCode("01A01");
        assertTrue(product.isArchived());
        assertNotNull(product.getPrimaryName());
        assertNotNull(product.getType());
        assertNotNull(product.getStrength());
    }

}
