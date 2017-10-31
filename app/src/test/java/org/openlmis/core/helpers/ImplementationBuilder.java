package org.openlmis.core.helpers;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.Treatment;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.openlmis.core.helpers.TreatmentBuilder.createDefaultTreatments;
import static org.openlmis.core.helpers.TreatmentBuilder.randomTreatment;
import static org.openlmis.core.utils.MalariaExecutors.APE;
import static org.openlmis.core.utils.MalariaExecutors.US;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Data
public class ImplementationBuilder {
    public static final Property<Implementation, Integer> id = new Property<>();
    public static final Property<Implementation, String> executor = new Property<>();
    public static final Property<Implementation, MalariaProgram> malariaProgram = new Property<>();
    public static final Property<Implementation, List<Treatment>> treatments = new Property<>();

    public static final Instantiator<Implementation> randomImplementation = new Instantiator<Implementation>() {
        @Override
        public Implementation instantiate(PropertyLookup<Implementation> lookup) {
            Implementation implementation = new Implementation(
                    lookup.valueOf(executor, randomAlphabetic(10)),
                    lookup.valueOf(treatments, newArrayList(make(a(randomTreatment)), make(a(randomTreatment)))));
            implementation.setMalariaProgram(lookup.valueOf(malariaProgram, (MalariaProgram) null));
            implementation.setId(lookup.valueOf(id, nextInt()));
            return implementation;
        }
    };

    public static List<Implementation> createDefaultImplementations() {
        List<Implementation> result = new ArrayList<>();
        result.add(make(a(randomImplementation, with(executor, US.name()), with(treatments, createDefaultTreatments()))));
        result.add(make(a(randomImplementation, with(executor, APE.name()), with(treatments, createDefaultTreatments()))));
        return result;
    }
}

