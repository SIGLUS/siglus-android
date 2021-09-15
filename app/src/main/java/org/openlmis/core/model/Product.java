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

package org.openlmis.core.model;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DatabaseTable(tableName = "products")
public class Product extends BaseModel implements Comparable<Product>, Serializable {

  public static final String MEDICINE_TYPE_ADULT = "Adult";
  public static final String MEDICINE_TYPE_CHILDREN = "Children";
  public static final String MEDICINE_TYPE_SOLUTION = "Solution";
  public static final String MEDICINE_TYPE_OTHER = "Other";
  public static final String MEDICINE_TYPE_DEFAULT = "Default";

  /**
   * @deprecated deprecated filed, use {@link ProductProgram}
   */
  @Deprecated
  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Program program;

  @DatabaseField
  String primaryName;

  @DatabaseField
  String strength;

  @DatabaseField
  String code;

  @DatabaseField
  String type;

  @SerializedName("archived")
  @DatabaseField
  boolean isArchived;

  @SerializedName("active")
  @DatabaseField
  boolean isActive;

  @DatabaseField
  boolean isKit;

  @DatabaseField
  boolean isBasic;

  @DatabaseField
  Double price;

  @DatabaseField(defaultValue = "false")
  boolean isHiv;

  @ForeignCollectionField()
  private ForeignCollection<Lot> lotList;

  private String additionalProgramCode;

  private transient List<KitProduct> kitProductList = new ArrayList<>();

  @Override
  public boolean equals(Object o) {
    if (o instanceof Product) {
      Product product = (Product) o;
      return product.getCode().equals(getCode());
    } else {
      return false;
    }
  }

  public String getFormattedProductName() {
    return getPrimaryName() + " [" + getCode() + "]";
  }

  public String getFormattedProductNameWithoutStrengthAndType() {
    return getProductNameWithoutStrengthAndType() + " [" + getCode() + "]";
  }

  public String getProductNameWithoutStrengthAndType() {
    return getPrimaryName().replace(getStrength() + getType(), "");
  }

  public String getProductFullName() {
    return getPrimaryName() + " [" + getCode() + "]" + getStrength() + getType();
  }

  public String getProductNameWithCodeAndStrength() {
    return getPrimaryName() + " [" + getCode() + "]" + getStrength();
  }

  @Override
  public int hashCode() {
    return getCode().hashCode();
  }

  @Override
  public int compareTo(@NonNull Product another) {
    return primaryName == null ? 0 : primaryName.compareTo(another.getPrimaryName());
  }

  public String getUnit() {
    return strength + " " + getType();
  }

  public enum IsKit {
    YES(true),
    NO(false);

    public boolean isKit() {
      return kit;
    }

    private final boolean kit;

    IsKit(boolean kit) {
      this.kit = kit;
    }
  }

  public static Product dummyProduct() {
    Product dummyProduct = new Product();
    dummyProduct.setCode("");
    dummyProduct.setPrimaryName("");
    dummyProduct.setStrength("");
    dummyProduct.setType("");
    return dummyProduct;
  }

  @Override
  public String toString() {
    return "[[code=" + code + ","
        + "type=" + type + ","
        + "isKit=" + isKit + ","
        + "isHiv=" + isHiv
        + "]"
        + super.toString()
        + "]";
  }
}
