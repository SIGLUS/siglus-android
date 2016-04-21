package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RegimenItem;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class RegimenItemRepositoryTest {

    private RegimenItemRepository regimenItemRepository;

    @Before
    public void setUp() throws Exception {
        regimenItemRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RegimenItemRepository.class);
    }

    @Test
    public void shouldDeleteSuccessful() throws Exception {
        RegimenItem regimenItem = new RegimenItem();
        regimenItemRepository.create(regimenItem);
        assertThat(regimenItemRepository.listAll().size(), is(1));

        regimenItemRepository.deleteRegimeItem(regimenItem);
        assertThat(regimenItemRepository.listAll().size(), is(0));
    }

    @Test
    public void shouldBatchCreateOrUpdateRegimenItems() throws Exception {
        List<RegimenItem> regimenItemList = Arrays.asList(
                new RegimenItem(), new RegimenItem(), new RegimenItem());

        regimenItemRepository.batchCreateOrUpdate(regimenItemList);

        assertEquals(3, regimenItemRepository.listAll().size());
    }

}