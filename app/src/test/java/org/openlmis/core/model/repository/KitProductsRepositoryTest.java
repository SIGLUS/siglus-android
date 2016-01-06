package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Kit;
import org.openlmis.core.model.KitProducts;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.KitBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LMISTestRunner.class)
public class KitProductsRepositoryTest extends LMISRepositoryUnitTest {

    private KitProductsRepository repository;

    private KitRepository kitRepository;

    private ProductRepository productRepository;


    @Before
    public void setUp() throws Exception {
        repository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(KitProductsRepository.class);
        kitRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(KitRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
    }

    @Test
    public void shouldSaveKitWhenKitNotExistInLocal() throws Exception {
        Kit kit = new KitBuilder()
                .setCode("US Kit Code")
                .setPrimaryName("US Kit Name")
                .build();
        repository.createOrUpdateKitWithProducts(Arrays.asList(kit));

        Kit queriedKit = kitRepository.getByCode(kit.getCode());

        assertThat(queriedKit.getPrimaryName()).isEqualTo("US Kit Name");
    }

    @Test
    public void shouldSuccessfullySaveKitProductsWhenProductsExistInLocal() throws Exception {
        Product firstProduct = new ProductBuilder().setCode("02A03").setPrimaryName("Hitrox de").setIsActive(true).setQuantityInKit(100).build();
        Product secondProduct = new ProductBuilder().setCode("02A04").setPrimaryName("Hitrox de hello").setIsActive(true).setQuantityInKit(200).build();

        productRepository.createOrUpdate(firstProduct);
        productRepository.createOrUpdate(secondProduct);

        Kit kit = new KitBuilder()
                .setCode("US Kit Code")
                .setPrimaryName("US Kit Name")
                .addProduct(firstProduct)
                .addProduct(secondProduct)
                .build();

        repository.createOrUpdateKitWithProducts(Arrays.asList(kit));

        List<KitProducts> kitProductsList = repository.getByKitId(kit.getId());

        assertThat(kitProductsList.size()).isEqualTo(2);
        assertThat(kitProductsList.get(0).getQuantity()).isEqualTo(firstProduct.getQuantityInKit());
    }

    @Test
    public void shouldUpdateKitProductsQuantityWhenKitProductsExisted() throws Exception {
        Product firstProduct = new ProductBuilder().setCode("02A03").setPrimaryName("Hitrox de").setIsActive(true).setQuantityInKit(100).build();
        productRepository.createOrUpdate(firstProduct);

        Kit kit = new KitBuilder()
                .setCode("US Kit Code")
                .setPrimaryName("US Kit Name")
                .addProduct(firstProduct)
                .build();

        repository.createOrUpdateKitWithProducts(Arrays.asList(kit));

        firstProduct.setQuantityInKit(200);

        repository.createOrUpdateKitWithProducts(Arrays.asList(kit));

        List<KitProducts> kitProductsList = repository.getByKitId(kit.getId());

        assertThat(kitProductsList.size()).isEqualTo(1);
        assertThat(kitProductsList.get(0).getQuantity()).isEqualTo(200);

    }
}