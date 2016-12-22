package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import java.util.Date;

public class ProgramDataFormPeriodService {
    @Inject
    StockRepository stockRepository;

    public Period getFirstStandardPeriod() throws LMISException {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            return Period.generateForTraining(new Date());
        }

        StockMovementItem firstStockMovement = stockRepository.getFirstStockMovement();
        if (firstStockMovement != null) {
            Period firstPeriod = firstStockMovement.getMovementPeriod();

            if (firstPeriod != null && todayEligibleForNewPeriod(firstPeriod)) {
                return firstPeriod;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean todayEligibleForNewPeriod(Period period) {
        DateTime periodBeginEligibleDate = period.getEnd().minusDays(Period.END_DAY - Period.INVENTORY_BEGIN_DAY);
        DateTime today = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        return periodBeginEligibleDate.isBefore(today) || periodBeginEligibleDate.equals(today);
    }

    public Period generateNextPeriod(Period period) {
        Period nextPeriod = period.next();
        if (todayEligibleForNewPeriod(nextPeriod)) {
            return nextPeriod;
        } else {
            return null;
        }
    }
}
