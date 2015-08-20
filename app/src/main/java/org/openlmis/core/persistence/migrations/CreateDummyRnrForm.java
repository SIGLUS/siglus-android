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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateDummyRnrForm implements Migration {

    DbUtil dbUtil;

    public CreateDummyRnrForm() {
        dbUtil = new DbUtil();
    }

    @Override
    public void up(SQLiteDatabase db, ConnectionSource connectionSource) {

        try {
            dbUtil.withDao(RnRForm.class, new DbUtil.Operation<RnRForm, String>() {
                @Override
                public String operate(Dao<RnRForm, String> dao) throws SQLException {
                    Program program = new Program();
                    program.setProgramCode("1");
                    Product product = new Product();
                    product.setProgram(program);
                    product.setId(1);
                    createRnrFormItem(product);
                    product.setId(2);
                    createRnrFormItem(product);
                    product.setId(3);
                    createRnrFormItem(product);
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    private List createRnrFormItem(Product product) {
        final List<RnrFormItem> list = new ArrayList();
        RnrFormItem rnrFormItem1 = new RnrFormItem();
        rnrFormItem1.setInventory(1000);
        rnrFormItem1.setIssued(1000);
        rnrFormItem1.setProduct(product);
        RnrFormItem rnrFormItem2 = new RnrFormItem();
        rnrFormItem2.setInventory(2000);
        rnrFormItem2.setIssued(2000);
        rnrFormItem2.setProduct(product);
        RnrFormItem rnrFormItem3 = new RnrFormItem();
        rnrFormItem3.setInventory(3000);
        rnrFormItem3.setIssued(3000);
        rnrFormItem3.setProduct(product);
        RnrFormItem rnrFormItem4 = new RnrFormItem();
        rnrFormItem4.setInventory(4000);
        rnrFormItem4.setIssued(4000);
        rnrFormItem4.setProduct(product);
        RnrFormItem rnrFormItem5 = new RnrFormItem();
        rnrFormItem5.setInventory(5000);
        rnrFormItem5.setIssued(5000);
        rnrFormItem5.setProduct(product);

        list.add(rnrFormItem1);
        list.add(rnrFormItem2);
        list.add(rnrFormItem3);
        list.add(rnrFormItem4);
        list.add(rnrFormItem5);

        try {
            dbUtil.withDaoAsBatch(RnrFormItem.class, new DbUtil.Operation<RnrFormItem, Void>() {
                @Override
                public Void operate(Dao<RnrFormItem, String> dao) throws SQLException {
                    for (RnrFormItem item : list) {
                        dao.create(item);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void down(SQLiteDatabase db, ConnectionSource connectionSource) {

    }
}
