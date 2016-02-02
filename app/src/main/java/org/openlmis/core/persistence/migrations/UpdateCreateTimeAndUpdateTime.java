package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class UpdateCreateTimeAndUpdateTime extends Migration {
    @Override
    public void up() {
        execSQL("UPDATE users SET createdAt = users.createdAt || ' 00:00:00' where length(users.createdAt)=10");
        execSQL("UPDATE program SET createdAt = program.createdAt || ' 00:00:00' where length(program.createdAt)=10");
        execSQL("UPDATE products SET createdAt = products.createdAt || ' 00:00:00' where length(products.createdAt)=10");
        execSQL("UPDATE stock_cards SET createdAt = stock_cards.createdAt || ' 00:00:00' where length(stock_cards.createdAt)=10");
        execSQL("UPDATE stock_items SET createdAt = stock_items.createdAt || ' 00:00:00' where length(stock_items.createdAt)=10");
        execSQL("UPDATE rnr_forms SET createdAt = rnr_forms.createdAt || ' 00:00:00' where length(rnr_forms.createdAt)=10");
        execSQL("UPDATE rnr_baseInfo_items SET createdAt = rnr_baseInfo_items.createdAt || ' 00:00:00' where length(rnr_baseInfo_items.createdAt)=10");
        execSQL("UPDATE rnr_form_items SET createdAt = rnr_form_items.createdAt || ' 00:00:00' where length(rnr_form_items.createdAt)=10");
        execSQL("UPDATE regimes SET createdAt = regimes.createdAt || ' 00:00:00' where length(regimes.createdAt)=10");
        execSQL("UPDATE regime_items SET createdAt = regime_items.createdAt || ' 00:00:00' where length(regime_items.createdAt)=10");
        execSQL("UPDATE kit_products SET createdAt = kit_products.createdAt || ' 00:00:00' where length(kit_products.createdAt)=10");
        execSQL("UPDATE draft_inventory SET createdAt = draft_inventory.createdAt || ' 00:00:00' where length(draft_inventory.createdAt)=10");
        execSQL("UPDATE sync_errors SET createdAt = sync_errors.createdAt || ' 00:00:00' where length(sync_errors.createdAt)=10");

        execSQL("UPDATE users SET updatedAt = users.updatedAt || ' 00:00:00' where length(users.updatedAt)=10");
        execSQL("UPDATE program SET updatedAt = program.updatedAt || ' 00:00:00' where length(program.updatedAt)=10");
        execSQL("UPDATE products SET updatedAt = products.updatedAt || ' 00:00:00' where length(products.updatedAt)=10");
        execSQL("UPDATE stock_cards SET updatedAt = stock_cards.updatedAt || ' 00:00:00' where length(stock_cards.updatedAt)=10");
        execSQL("UPDATE stock_items SET updatedAt = stock_items.updatedAt || ' 00:00:00' where length(stock_items.updatedAt)=10");
        execSQL("UPDATE rnr_forms SET updatedAt = rnr_forms.updatedAt || ' 00:00:00' where length(rnr_forms.updatedAt)=10");
        execSQL("UPDATE rnr_baseInfo_items SET updatedAt = rnr_baseInfo_items.updatedAt || ' 00:00:00' where length(rnr_baseInfo_items.updatedAt)=10");
        execSQL("UPDATE rnr_form_items SET updatedAt = rnr_form_items.updatedAt || ' 00:00:00' where length(rnr_form_items.updatedAt)=10");
        execSQL("UPDATE regimes SET updatedAt = regimes.updatedAt || ' 00:00:00' where length(regimes.updatedAt)=10");
        execSQL("UPDATE regime_items SET updatedAt = regime_items.updatedAt || ' 00:00:00' where length(regime_items.updatedAt)=10");
        execSQL("UPDATE kit_products SET updatedAt = kit_products.updatedAt || ' 00:00:00' where length(kit_products.updatedAt)=10");
        execSQL("UPDATE draft_inventory SET updatedAt = draft_inventory.updatedAt || ' 00:00:00' where length(draft_inventory.updatedAt)=10");
        execSQL("UPDATE sync_errors SET updatedAt = sync_errors.updatedAt || ' 00:00:00' where length(sync_errors.updatedAt)=10");
    }
}