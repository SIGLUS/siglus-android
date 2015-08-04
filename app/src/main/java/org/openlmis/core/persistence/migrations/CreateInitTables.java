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

package org.openlmis.core.persistence.migrations;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;


import org.openlmis.core.model.MIMIAForm;
import org.openlmis.core.model.MIMIAProductItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import org.openlmis.core.model.User;
import org.openlmis.core.persistence.Migration;

import java.sql.SQLException;

import static com.j256.ormlite.table.TableUtils.createTable;

public class CreateInitTables implements Migration {
    @Override
    public void up(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            createTable(connectionSource, User.class);
            createTable(connectionSource, Product.class);
            createTable(connectionSource, StockCard.class);
            createTable(connectionSource, StockItem.class);
            createTable(connectionSource, MIMIAForm.class);
            createTable(connectionSource, MIMIAProductItem.class);
            createTable(connectionSource, Regimen.class);
            createTable(connectionSource, RegimenItem.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void down(SQLiteDatabase db, ConnectionSource connectionSource) {

    }
}
