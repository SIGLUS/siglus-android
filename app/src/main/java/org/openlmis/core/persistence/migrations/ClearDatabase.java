package org.openlmis.core.persistence.migrations;

import android.content.Context;
import android.content.SharedPreferences;
import org.openlmis.core.LMISApp;
import org.openlmis.core.persistence.Migration;

public class ClearDatabase extends Migration {

  @Override
  public void up() {
    execSQL("DELETE FROM cmm");
    execSQL("DELETE FROM dirty_data");
    execSQL("DELETE FROM draft_initial_lot_items");
    execSQL("DELETE FROM draft_initial_inventory");
    execSQL("DELETE FROM draft_lot_items");
    execSQL("DELETE FROM draft_inventory");
    execSQL("DELETE FROM health_facility_service");
    execSQL("DELETE FROM implementation");
    execSQL("DELETE FROM inventory");
    execSQL("DELETE FROM kit_products");
    execSQL("DELETE FROM lot_movement_items");
    execSQL("DELETE FROM lots_on_hand");
    execSQL("DELETE FROM malaria_program");
    execSQL("DELETE FROM patient_dispensation");
    execSQL("DELETE FROM product_programs");
    execSQL("DELETE FROM program_data_Basic_items");
    execSQL("DELETE FROM program_data_columns");
    execSQL("DELETE FROM program_data_form_signatures");
    execSQL("DELETE FROM program_data_items");
    execSQL("DELETE FROM program_data_forms");
    execSQL("DELETE FROM service_dispensation");
    execSQL("DELETE FROM ptv_program_stock_information");
    execSQL("DELETE FROM ptv_program");
    execSQL("DELETE FROM regime_items");
    execSQL("DELETE FROM regime_short_code");
    execSQL("DELETE FROM regime_three_lines");
    execSQL("DELETE FROM reports_type");
    execSQL("DELETE FROM rnr_baseInfo_items");
    execSQL("DELETE FROM rnr_form_items");
    execSQL("DELETE FROM rnr_form_signature");
    execSQL("DELETE FROM rnr_forms");
    execSQL("DELETE FROM service_items");
    execSQL("DELETE FROM services");
    execSQL("DELETE FROM stock_items");
    execSQL("DELETE FROM stock_cards");
    execSQL("DELETE FROM sync_errors");
    execSQL("DELETE FROM treatment");
    execSQL("DELETE FROM users");
    execSQL("DELETE FROM programs");
    execSQL("DELETE FROM products");
    execSQL("DELETE FROM lots");
    execSQL("UPDATE sqlite_sequence set seq = 0 where name != 'regimes'");
    clearSharedPreference();

  }

  private void clearSharedPreference() {
    SharedPreferences preferences = LMISApp.getContext().getSharedPreferences("LMISPreference", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.clear();
    editor.apply();
  }
}
