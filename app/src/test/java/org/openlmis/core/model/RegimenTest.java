package org.openlmis.core.model;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class RegimenTest {

    private Regimen regimen;

    @Before
    public void setUp() throws Exception {
        regimen = new Regimen();
    }

    @Test
    public void shouldReturnFalseIfRegimenIsDefault() throws Exception {
        regimen.setName("ABC+3TC+EFZ");
        assertFalse(regimen.isCustom());
    }

    @Test
    public void shouldReturnTrueIfRegimenIsCustom() throws Exception {
        regimen.setName("custom");
        assertTrue(regimen.isCustom());
    }
}