package org.openlmis.core.helpers;


import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Treatment;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.openlmis.core.helpers.ProductBuilder.product6x1;
import static org.openlmis.core.helpers.ProductBuilder.product6x2;
import static org.openlmis.core.helpers.ProductBuilder.product6x3;
import static org.openlmis.core.helpers.ProductBuilder.product6x4;
import static org.openlmis.core.helpers.ProductBuilder.randomProduct;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class TreatmentBuilder {
    public static final Property<Treatment, Product> product = new Property<>();
    public static final Property<Treatment, Integer> amount = new Property<>();
    public static final Property<Treatment, Integer> stock = new Property<>();
    public static final Property<Treatment, Implementation> implementation = new Property<>();

    public static final Instantiator<Treatment> randomTreatment = new Instantiator<Treatment>() {
        @Override
        public Treatment instantiate(PropertyLookup<Treatment> lookup) {
            Treatment treatment = new Treatment(
                    lookup.valueOf(product, make(a(randomProduct))),
                    lookup.valueOf(amount, nextInt(100)),
                    lookup.valueOf(stock, nextInt(100)));
            Implementation nullImplementation = null;
            treatment.setImplementation(lookup.valueOf(implementation, nullImplementation));
            return treatment;
        }
    };

    public static final Instantiator<Treatment> treatment6x1 = new Instantiator<Treatment>() {
        @Override
        public Treatment instantiate(PropertyLookup<Treatment> lookup) {
            Treatment treatment = new Treatment(
                    lookup.valueOf(product, make(a(product6x1))),
                    lookup.valueOf(amount, nextInt(100)),
                    lookup.valueOf(stock, nextInt(100)));
            Implementation nullImplementation = null;
            treatment.setImplementation(lookup.valueOf(implementation, nullImplementation));
            return treatment;
        }
    };

    public static final Instantiator<Treatment> treatment6x2 = new Instantiator<Treatment>() {
        @Override
        public Treatment instantiate(PropertyLookup<Treatment> lookup) {
            Treatment treatment = new Treatment(
                    lookup.valueOf(product, make(a(product6x2))),
                    lookup.valueOf(amount, nextInt(100)),
                    lookup.valueOf(stock, nextInt(100)));
            Implementation nullImplementation = null;
            treatment.setImplementation(lookup.valueOf(implementation, nullImplementation));
            return treatment;
        }
    };

    public static final Instantiator<Treatment> treatment6x3 = new Instantiator<Treatment>() {
        @Override
        public Treatment instantiate(PropertyLookup<Treatment> lookup) {
            Treatment treatment = new Treatment(
                    lookup.valueOf(product, make(a(product6x3))),
                    lookup.valueOf(amount, nextInt(100)),
                    lookup.valueOf(stock, nextInt(100)));
            Implementation nullImplementation = null;
            treatment.setImplementation(lookup.valueOf(implementation, nullImplementation));
            return treatment;
        }
    };

    public static final Instantiator<Treatment> treatment6x4 = new Instantiator<Treatment>() {
        @Override
        public Treatment instantiate(PropertyLookup<Treatment> lookup) {
            Treatment treatment = new Treatment(
                    lookup.valueOf(product, make(a(product6x4))),
                    lookup.valueOf(amount, nextInt(100)),
                    lookup.valueOf(stock, nextInt(100)));
            Implementation nullImplementation = null;
            treatment.setImplementation(lookup.valueOf(implementation, nullImplementation));
            return treatment;
        }
    };

    public static List<Treatment> createDefaultTreatments() {
        return newArrayList(make(a(treatment6x1)),
                make(a(treatment6x2)),
                make(a(treatment6x3)),
                make(a(treatment6x4)));
    }
}


