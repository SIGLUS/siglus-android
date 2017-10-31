package org.openlmis.core.helpers;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import org.openlmis.core.model.Product;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.natpryce.makeiteasy.Property.newProperty;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x1_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x2_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x3_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x4_CODE;

public class ProductBuilder {

    public static final Long PRODUCT_ID = 0L;
    public static final Property<Product, Long> productId = newProperty();
    public static final Property<Product, String> code = newProperty();
    public static final Property<Product, String> primaryName = newProperty();
    public static final Property<Product, Boolean> active = newProperty();

    public static final Instantiator<Product> randomProduct = new Instantiator<Product>() {
        @Override
        public Product instantiate(PropertyLookup<Product> lookup) {
            Product product = new Product();
            product.setId(lookup.valueOf(productId, PRODUCT_ID));
            product.setCode(lookup.valueOf(code, randomAlphabetic(5)));
            product.setActive(lookup.valueOf(active, true));
            product.setPrimaryName(lookup.valueOf(primaryName, randomAlphabetic(10)));
            product.setType(randomAlphabetic(10));
            product.setStrength("strength");
            return product;
        }
    };

    public static final Instantiator<Product> product6x1 = new Instantiator<Product>() {
        @Override
        public Product instantiate(PropertyLookup<Product> lookup) {
            return make(a(randomProduct, with(code, PRODUCT_6x1_CODE.getValue())));
        }
    };

    public static final Instantiator<Product> product6x2 = new Instantiator<Product>() {
        @Override
        public Product instantiate(PropertyLookup<Product> lookup) {
            return make(a(randomProduct, with(code, PRODUCT_6x2_CODE.getValue())));
        }
    };

    public static final Instantiator<Product> product6x3 = new Instantiator<Product>() {
        @Override
        public Product instantiate(PropertyLookup<Product> lookup) {
            return make(a(randomProduct, with(code, PRODUCT_6x3_CODE.getValue())));
        }
    };

    public static final Instantiator<Product> product6x4 = new Instantiator<Product>() {
        @Override
        public Product instantiate(PropertyLookup<Product> lookup) {
            return make(a(randomProduct, with(code, PRODUCT_6x4_CODE.getValue())));
        }
    };
}
