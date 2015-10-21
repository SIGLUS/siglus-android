package org.openlmis.core.persistence.migrations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.StockMovementItem;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(LMISTestRunner.class)
public class ChangeMovementReasonToCodeTest {

    ChangeMovementReasonToCode migrate;

    @Before
    public void setUp() throws Exception {
        migrate = new ChangeMovementReasonToCode();
    }


    @Test
    public void shouldSetDefaultCodeWhenCannotFindACodeForReason(){
        StockMovementItem item = new StockMovementItem();
        item.setMovementType(StockMovementItem.MovementType.ISSUE);
        item.setReason("cannot find a code");

        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is("DEFAULT_ISSUE"));

        item.setMovementType(StockMovementItem.MovementType.RECEIVE);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is("DEFAULT_RECEIVE"));

        item.setMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is("DEFAULT_POSITIVE_ADJUSTMENT"));

        item.setMovementType(StockMovementItem.MovementType.NEGATIVE_ADJUST);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is("DEFAULT_NEGATIVE_ADJUSTMENT"));

        item.setMovementType(StockMovementItem.MovementType.PHYSICAL_INVENTORY);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is("INVENTORY"));
    }
}