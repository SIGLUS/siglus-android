package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.roboguice.shaded.goole.common.base.Optional;

public class ProgramDataFormPeriodService {
    @Inject
    StockRepository stockRepository;
    @Inject
    private StockMovementRepository stockMovementRepository;

    public Optional<Period> getFirstStandardPeriod() throws LMISException {
        StockMovementItem firstStockMovement = stockMovementRepository.getFirstStockMovement();
        if (firstStockMovement != null) {
            Period firstPeriod = firstStockMovement.getMovementPeriod();

            if (firstPeriod != null && firstPeriod.isOpenToRequisitions()) {
                return Optional.of(firstPeriod);
            }
        }
        return Optional.absent();
    }
}
