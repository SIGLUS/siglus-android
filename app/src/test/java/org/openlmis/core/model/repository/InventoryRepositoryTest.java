package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class InventoryRepositoryTest {


    private InventoryRepository repository;

    @Before
    public void setup() throws LMISException {
        repository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(InventoryRepository.class);
    }

    @Test
    public void shouldQueryPeriodInventory() throws Exception {
        repository.save(getInventory(DateUtil.parseString("2016-01-22 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-01 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-18 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-22 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-25 11:33:44", DateUtil.DATE_TIME_FORMAT)));
        repository.save(getInventory(DateUtil.parseString("2016-02-26 11:33:44", DateUtil.DATE_TIME_FORMAT)));

        List<Inventory> inventories = repository.queryPeriodInventory(new Period(new DateTime(DateUtil.parseString("2016-02-28 11:33:44", DateUtil.DATE_TIME_FORMAT))));

        assertThat(inventories.size(), is(3));
        assertThat(inventories.get(0).getCreatedAt(), is(DateUtil.parseString("2016-02-25 11:33:44", DateUtil.DATE_TIME_FORMAT)));
    }

    @NonNull
    private Inventory getInventory(Date date) {
        Inventory inventory = new Inventory();
        inventory.setCreatedAt(date);
        inventory.setUpdatedAt(date);
        return inventory;
    }
}