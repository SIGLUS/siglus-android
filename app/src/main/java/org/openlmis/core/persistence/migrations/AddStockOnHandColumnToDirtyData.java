package org.openlmis.core.persistence.migrations;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.openlmis.core.persistence.Migration;

public class AddStockOnHandColumnToDirtyData extends Migration {

  @Override
  public void up() {
    if (!isColumnExists(this.db, "dirty_data", "stockOnHand")) {
      execSQL("ALTER TABLE 'dirty_data' ADD COLUMN stockOnHand BIGINT");
    }
  }

  private boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
    Cursor cursor = null;
    try {
      cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
      while (cursor.moveToNext()) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        if (name.equals(columnName)) {
          return true;
        }
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return false;
  }
}
