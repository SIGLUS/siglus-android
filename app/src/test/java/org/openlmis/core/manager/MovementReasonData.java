/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.manager;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.EnumMap;
import org.openlmis.core.manager.MovementReasonManager.MovementType;

public class MovementReasonData {
  public static EnumMap<MovementType, ArrayList<String>> PT_TYPE_TO_DESC_LIST = new EnumMap<>(MovementType.class);
  public static EnumMap<MovementType, ArrayList<String>> EN_TYPE_TO_DESC_LIST = new EnumMap<>(MovementType.class);

  static {
    // init pt
    PT_TYPE_TO_DESC_LIST.put(MovementType.RECEIVE, newArrayList("Distrito (DDM)",
        "Província (DPM)",
        "Intermediate Warehouse ( AI)"));
    PT_TYPE_TO_DESC_LIST.put(MovementType.ISSUE, newArrayList("Farmácia Pública",
        "Maternidade-SMI",
        "Enfermaria",
        "Banco de Socorro-BIS",
        "Brigada móvel",
        "Laboratório",
        "UATS",
        "PNCTL",
        "PAV",
        "Estomatologia",
        "Desembalar Kit"));
    PT_TYPE_TO_DESC_LIST
        .put(MovementType.NEGATIVE_ADJUST, newArrayList("Devolução de Expirados Quarentena (ou Depósito fornecedor)",
            "Danificados no Depósito",
            "Empréstimo (de todos os Níveis) que dão saída do Depósito",
            "Correção do Inventário, no caso de excesso de stock (stock é superior ao existente na ficha)",
            "Saída para Quarentena no Caso de Problemas relativos as Qualidade",
            "Devolução para o depósito fornecedor"));
    PT_TYPE_TO_DESC_LIST
        .put(MovementType.POSITIVE_ADJUST, newArrayList("Devolução dos seus dependentes (US e Depósitos Beneficiários)",
            "Devolução de Expirados (US e Depósitos Beneficiários)",
            "Doação ao Depósito",
            "Empréstimo (de todos os Níveis) que dão entrada no Depósito",
            "Correção do Inventário, no caso de stock em falta (stock é inferior ao existente na ficha)",
            "Retorno da Quarentena no caso de se confirmar a qualidade do produto"));
    PT_TYPE_TO_DESC_LIST.put(MovementType.PHYSICAL_INVENTORY, newArrayList("Inventario",
        "Correção do Inventário, no caso de excesso de stock (stock é superior ao existente na ficha)",
        "Correção do Inventário, no caso de stock em falta (stock é inferior ao existente na ficha)"));
    // init en
    EN_TYPE_TO_DESC_LIST.put(MovementType.RECEIVE, newArrayList("District( DDM)",
        "Province ( DPM)",
        "Intermediate Warehouse ( AI)"));
    EN_TYPE_TO_DESC_LIST.put(MovementType.ISSUE, newArrayList("Public pharmacy",
        "Maternity",
        "General Ward",
        "Accident & Emergency",
        "Mobile unit",
        "Laboratory",
        "UATS",
        "PNCTL",
        "PAV",
        "Dental ward",
        "Unpack kit"));
    EN_TYPE_TO_DESC_LIST
        .put(MovementType.NEGATIVE_ADJUST, newArrayList("Drugs in quarantine have expired, returned to Supplier",
            "Damaged on arrival",
            "Loans made from a health facility deposit",
            "Inventory correction in case of over stock on Stock card (Stock on hand is less than stock in stock card)",
            "Product defective, moved to quarantine",
            "Return to DDM"));
    EN_TYPE_TO_DESC_LIST.put(MovementType.POSITIVE_ADJUST, newArrayList("Returns from Customers(HF and dependent wards)",
        "Returns of expired drugs (HF and dependent wards)",
        "Donations to Deposit",
        "Loans received at the health facility deposit",
        "Inventory correction in case of under stock on Stock card (Stock on hand is more than stock in stock card)",
        "Returns from Quarantine, in the case of quarantined product being fit for use"));
    EN_TYPE_TO_DESC_LIST.put(MovementType.PHYSICAL_INVENTORY, newArrayList("Inventory",
        "Inventory correction in case of over stock on Stock card (Stock on hand is less than stock in stock card)",
        "Inventory correction in case of under stock on Stock card (Stock on hand is more than stock in stock card)"));
  }
}
