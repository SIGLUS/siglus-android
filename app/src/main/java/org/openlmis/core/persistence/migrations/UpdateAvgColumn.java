package org.openlmis.core.persistence.migrations;

import org.openlmis.core.LMISApp;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.persistence.Migration;

import roboguice.RoboGuice;

public class UpdateAvgColumn extends Migration {
    StockService stockService;

    public UpdateAvgColumn() {
        this.stockService = RoboGuice.getInjector(LMISApp.getContext()).getInstance(StockService.class);
    }

    @Override
    public void up() {
        stockService.immediatelyUpdateAvgMonthlyConsumption();
    }
}
