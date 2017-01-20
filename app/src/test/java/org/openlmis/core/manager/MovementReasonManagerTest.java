package org.openlmis.core.manager;


import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;


@RunWith(LMISTestRunner.class)
public class MovementReasonManagerTest {

    MovementReasonManager reasonManager;
    ArrayList<String> reasonDescListPT;
    ArrayList<String> reasonDescListEN;

    @Before
    public void setUp() throws Exception {
        reasonManager = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MovementReasonManager.class);

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
                "Estomatologia",
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
                "Retorno da Quarentena no caso de se confirmar a qualidade do produto");


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
                "Loans received at the health facility deposit",
                "Inventory correction in case of under stock on Stock card (Stock on hand is more than stock in stock card)",
                "Returns from Quarantine, in the case of quarantined product being fit for use");
    }

    @Test
    @Config(qualifiers="en-port")
    public void shouldGetRightCodeFromEnglishReason() throws Exception {

        ArrayList<String> totalList = newArrayList();
        totalList.addAll(reasonDescListEN);

        for (String reason : totalList){
            String code = reasonManager.queryByDesc(reason).getCode();
            assertThat(StringUtils.isEmpty(code), is(false));
        }
    }

    @Test
    @Config(qualifiers="pt-port")
    public void shouldGetRightCodeForPTReason() throws Exception {
        ArrayList<String> totalList = newArrayList();
        totalList.addAll(reasonDescListPT);

        for (String reason : totalList){
            String code = reasonManager.queryByDesc(reason).getCode();
            assertThat(StringUtils.isEmpty(code), is(false));
        }
    }

    @Test
    public void shouldNotDisplayPhysicalInventoryAndDefaultReasonOnMenu() throws MovementReasonNotFoundException{
        assertThat(reasonManager.queryByCode(MovementReasonManager.INVENTORY).canBeDisplayOnMovementMenu(), is(false));
        assertThat(reasonManager.queryByCode(MovementReasonManager.DEFAULT_ISSUE).canBeDisplayOnMovementMenu(), is(false));
        assertThat(reasonManager.queryByCode(MovementReasonManager.DEFAULT_RECEIVE).canBeDisplayOnMovementMenu(), is(false));
        assertThat(reasonManager.queryByCode(MovementReasonManager.DEFAULT_NEGATIVE_ADJUSTMENT).canBeDisplayOnMovementMenu(), is(false));
        assertThat(reasonManager.queryByCode(MovementReasonManager.DEFAULT_POSITIVE_ADJUSTMENT).canBeDisplayOnMovementMenu(), is(false));
        assertThat(reasonManager.queryByCode(MovementReasonManager.DONATION).canBeDisplayOnMovementMenu(),is(false));
        assertThat(reasonManager.queryByCode(MovementReasonManager.UNPACK_KIT).canBeDisplayOnMovementMenu(),is(false));
    }

    @Test
    public void shouldDisplayOnMovementReasonMenu() throws MovementReasonNotFoundException{
        for (String reason : reasonDescListEN){
            if (reason.equalsIgnoreCase("Inventory")){
                continue;
            }
            assertThat(reasonManager.queryByDesc(reason).canBeDisplayOnMovementMenu(), is(true));
        }
    }
}