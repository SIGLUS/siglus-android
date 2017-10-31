package org.openlmis.core.model;

import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openlmis.core.helpers.ImplementationBuilder.randomImplementation;

public class ImplementationTest {

    @Test
    public void shouldReturnTrueWhenExecutorMatches() throws Exception {
        Implementation implementation = make(a(randomImplementation));
        assertThat(implementation.isExecutor(implementation.getExecutor()), is(true));
    }

    @Test
    public void shouldReturnFalseWhenExecutorDoesNotMatch() throws Exception {
        Implementation implementation = make(a(randomImplementation));
        assertThat(implementation.isExecutor(randomAlphabetic(4)), is(false));
    }
}