package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

/**
 * Migration to recalculate calculatedOrderQuantity for all existing rnr_form_items.
 * 
 * This ensures that existing forms in the database (which may have null or zero values
 * from server sync) get the correct calculated value using the formula:
 * calculatedOrderQuantity = MAX(0, (issued * 2) - inventory)
 * 
 * This is needed because the fix in RnrFormRepository only applies to newly saved forms,
 * not existing data in the database.
 */
public class RecalculateOrderQuantityForExistingRnrFormItems extends Migration {

  @Override
  public void up() {
    // Recalculate calculatedOrderQuantity for all existing rnr_form_items
    // Formula: calculatedOrderQuantity = MAX(0, (issued * 2) - inventory)
    // Handle null values by treating them as 0
    execSQL(
        "UPDATE rnr_form_items "
        + "SET calculatedOrderQuantity = "
        + "CASE "
        + "  WHEN ((COALESCE(issued, 0) * 2) - COALESCE(inventory, 0)) > 0 "
        + "  THEN (COALESCE(issued, 0) * 2) - COALESCE(inventory, 0) "
        + "  ELSE 0 "
        + "END"
    );
  }
}
