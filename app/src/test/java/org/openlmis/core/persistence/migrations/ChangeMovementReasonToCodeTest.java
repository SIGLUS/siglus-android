package org.openlmis.core.persistence.migrations;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class ChangeMovementReasonToCodeTest extends LMISRepositoryUnitTest{

    ChangeMovementReasonToCode migrate;
    private ArrayList<String> reasonDescListPT;
    private ArrayList<String> reasonDescListEN;

    @Before
    public void setUp() throws Exception {
        migrate = new ChangeMovementReasonToCode();
        migrate.stockItemGenericDao = spy(migrate.stockItemGenericDao);
        migrate.dbUtil = mock(DbUtil.class);

        reasonDescListPT = newArrayList("Inventario",
                "Distrito (DDM)",
                "Província (DPM)",
                "Farmácia Pública",
                "Maternidade-SMI",
                "Enfermaria",
                "Banco de Socorro-BIS",
                "Brigada móvel",
                "Laboratório",
                "UATS",
                "PNCTL",
                "PAV",
                "Estomalogia",
                "Devolução de Expirados Quarentena (ou Depósito fornecedor)",
                "Danificados no Depósito",
                "Empréstimo (de todos os Níveis) que dão saída do Depósito",
                "Correção do Inventário, no caso de excesso de stock (stock é superior ao existente na ficha)",
                "Saída para Quarentena no Caso de Problemas relativos as Qualidade",
                "Devolução dos seus dependentes (US e Depósitos Beneficiários)",
                "Devolução de Expirados (US e Depósitos Beneficiários)",
                "Doação ao Depósito",
                "Empréstimo (de todos os Níveis) que dão entrada no Depósito",
                "Correção do Inventário, no caso de stock em falta (stock é inferior ao existente na ficha)",
                "Retorno da Quarentena no caso de se confirmar a qualidade do produto",
                "physicalInventoryPositive",
                "physicalInventoryNegative");


        reasonDescListEN = newArrayList("Inventory",
                "District( DDM)",
                "Province ( DPM)",
                "Public pharmacy",
                "Maternity",
                "General Ward",
                "Accident & Emergency",
                "Mobile unit",
                "Laboratory",
                "UATS",
                "PNCTL",
                "PAV",
                "Dental ward",
                "Drugs in quarantine have expired, returned to Supplier",
                "Damaged on arrival",
                "Loans made from a health facility deposit",
                "Inventory correction in case of over stock on Stock  card (Stock on  hand is less than stock in stock card)",
                "Product defective, moved to quarantine",
                "Returns from Customers(HF and dependent wards)",
                "Returns of expired drugs (HF and dependent wards)",
                "Donations to Deposit",
                "Loans received at the health facility deposit",
                "Inventory correction in case of under stock on Stock card (Stock on hand is more than stock in stock card)",
                "Returns from Quarantine, in the case of quarantined product being fit for use",
                "physicalInventoryPositive",
                "physicalInventoryNegative");
    }


    @Test
    public void shouldSetDefaultCodeWhenCannotFindACodeForReason(){
        StockMovementItem item = new StockMovementItem();
        item.setMovementType(MovementReasonManager.MovementType.ISSUE);
        item.setReason("cannot find a code");

        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_ISSUE));

        item.setMovementType(MovementReasonManager.MovementType.RECEIVE);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_RECEIVE));

        item.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_POSITIVE_ADJUSTMENT));

        item.setMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is(MovementReasonManager.DEFAULT_NEGATIVE_ADJUSTMENT));

        item.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is(MovementReasonManager.INVENTORY));

        item.setReason("physicalInventoryPositive");
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is(MovementReasonManager.INVENTORY_POSITIVE));

        item.setReason("physicalInventoryNegative");
        migrate.setDefaultReasonCode(item);
        assertThat(item.getReason(), is(MovementReasonManager.INVENTORY_NEGATIVE));

    }

    @Test
    @Config(qualifiers="pt-port")
    public void shouldReplaceLegacyReasonDataToReasonCodePT() throws LMISException{
        MovementReasonManager.getInstance().refresh();
        testMigrate(reasonDescListPT);
    }


    @Test
    @Config(qualifiers="en-port")
    public void shouldReplaceLegacyReasonDataToReasonCodeEN() throws LMISException{
        MovementReasonManager.getInstance().refresh();
        testMigrate(reasonDescListEN);
    }

    private void testMigrate(ArrayList<String> reasonDescList) throws LMISException {
        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

        for (String reason : reasonDescList){
            StockMovementItem item = new StockMovementItem();
            item.setReason(reason);
            stockMovementItems.add(item);
        }
        doReturn(stockMovementItems).when(migrate.stockItemGenericDao).queryForAll();

        migrate.up();

        for (StockMovementItem newItem : stockMovementItems){
            System.out.println("ReasonCode:" + newItem.getReason());
            assertThat(newItem.getReason().isEmpty(), is(false));
        }
    }
}