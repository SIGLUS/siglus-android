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

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Data;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.persistence.migrations.ChangeMovementReasonToCode;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Singleton
public final class MovementReasonManager {

  public static final String INVENTORY_POSITIVE = "INVENTORY_POSITIVE";
  public static final String INVENTORY_NEGATIVE = "INVENTORY_NEGATIVE";
  public static final String INVENTORY = "INVENTORY";
  public static final String UNPACK_KIT = "UNPACK_KIT";
  public static final String DONATION = "DONATION";
  public static final String DDM = "DISTRICT_DDM";

  Context context;
  public static final String RES_DIVIDER = "[|]";

  List<MovementReason> currentReasonList;
  List<MovementType> typeList;

  private static MovementReasonManager instance;

  Map<String, ArrayList<MovementReason>> reasonCache;
  Map<String, ArrayList<MovementType>> typeCache;

  @Inject
  private MovementReasonManager(Context context) {
    this.context = context;
    reasonCache = new HashMap<>();
    typeCache = new HashMap<>();
    currentReasonList = initReasonList(this.context.getResources().getConfiguration().locale);
    typeList = initTypeList(this.context.getResources().getConfiguration().locale);
  }

  public static synchronized MovementReasonManager getInstance() {
    if (instance == null) {
      instance = new MovementReasonManager(LMISApp.getContext());
    }
    return instance;
  }


  public synchronized void refresh() {
    instance = new MovementReasonManager(LMISApp.getContext());
  }

  private ArrayList<MovementType> initTypeList(Locale locale) {
    if (typeCache.containsKey(locale.getLanguage())) {
      return typeCache.get(locale.getLanguage());
    }
    MovementType.ISSUE.description = getResourceByLocal(locale).getString(R.string.ISSUE);
    MovementType.RECEIVE.description = getResourceByLocal(locale).getString(R.string.RECEIVE);
    MovementType.POSITIVE_ADJUST.description = getResourceByLocal(locale).getString(R.string.POSITIVE_ADJUST);
    MovementType.NEGATIVE_ADJUST.description = getResourceByLocal(locale).getString(R.string.NEGATIVE_ADJUST);

    ArrayList<MovementType> typeArrayList = new ArrayList<>();
    typeArrayList.add(MovementType.ISSUE);
    typeArrayList.add(MovementType.RECEIVE);
    typeArrayList.add(MovementType.NEGATIVE_ADJUST);
    typeArrayList.add(MovementType.POSITIVE_ADJUST);

    typeCache.put(locale.getLanguage(), typeArrayList);
    return typeArrayList;
  }

  private ArrayList<MovementReason> initReasonList(Locale locale) {
    if (reasonCache.containsKey(locale.getLanguage())) {
      return reasonCache.get(locale.getLanguage());
    }

    String[] reasonData = getResourceByLocal(locale).getStringArray(R.array.reason_data);
    ArrayList<MovementReason> reasonList = parseReasonListFromConfig(reasonData);
    reasonCache.put(locale.getLanguage(), reasonList);
    return reasonList;
  }

  @NonNull
  private static ArrayList<MovementReason> parseReasonListFromConfig(String[] reasonData) {
    ArrayList<MovementReason> reasonList = new ArrayList<>();
    for (String s : reasonData) {
      String[] values = s.split(RES_DIVIDER);

      if (values.length < 3) {
        Log.d(MovementReasonManager.class.getSimpleName(), "Invalid Config =>" + s);
        continue;
      }

      MovementReason reason = new MovementReason(MovementType.valueOf(values[0]), values[1], values[2]);
      reasonList.add(reason);
    }
    return reasonList;
  }

  public List<MovementReason> buildReasonListForMovementType(final MovementType type) {
    return FluentIterable.from(currentReasonList)
        .filter(movementReason -> movementReason.getMovementType() == type
            && movementReason.canBeDisplayOnMovementMenu())
        .toList();
  }

  public MovementReason queryByDesc(MovementType movementType, String reason) throws MovementReasonNotFoundException {
    return queryByDesc(movementType, reason, context.getResources().getConfiguration().locale);
  }

  public MovementReason queryByDesc(MovementType movementType, String desc, Locale locale)
      throws MovementReasonNotFoundException {
    ArrayList<MovementReason> reasonList = initReasonList(locale);
    Optional<MovementReason> matched = FluentIterable.from(reasonList)
        .firstMatch(movementReason -> {
          if (movementReason == null) {
            return false;
          }
          boolean isReasonMatch = movementReason.getDescription().equalsIgnoreCase(desc);
          boolean isTypeMatch = movementReason.getMovementType() == movementType;
          return isReasonMatch && isTypeMatch;
        });

    if (!matched.isPresent()) {
      throw new MovementReasonNotFoundException(desc);
    }
    return matched.get();
  }

  public MovementReason queryByCode(MovementType movementType, String code) throws MovementReasonNotFoundException {
    Optional<MovementReason> matched = FluentIterable.from(currentReasonList)
        .firstMatch(movementReason -> {
          if (movementReason == null) {
            return false;
          }
          boolean isCodeMatch = movementReason.getCode().equalsIgnoreCase(code);
          boolean isTypeMatch = movementReason.getMovementType() == movementType;
          return isCodeMatch && isTypeMatch;
        });
    if (!matched.isPresent()) {
      throw new MovementReasonNotFoundException(code);
    }
    return matched.get();
  }

  public Resources getResourceByLocal(Locale locale) {
    Resources standardResources = context.getResources();
    AssetManager assets = standardResources.getAssets();
    DisplayMetrics metrics = standardResources.getDisplayMetrics();
    Configuration config = new Configuration(standardResources.getConfiguration());
    config.locale = locale;
    return new Resources(assets, metrics, config);
  }

  public List<MovementType> getMovementTypes() {
    return typeList;
  }

  public enum MovementType {
    RECEIVE("RECEIVE"),
    ISSUE("ISSUE"),
    POSITIVE_ADJUST("POSITIVE_ADJUST"),
    NEGATIVE_ADJUST("NEGATIVE_ADJUST"),
    PHYSICAL_INVENTORY("PHYSICAL_INVENTORY"),
    INITIAL_INVENTORY("INITIAL_INVENTORY"),
    DEFAULT("default"),
    ADJUSTMENT("ADJUSTMENT"),
    REJECTION("REJECTION");

    private final String value;

    String description;

    MovementType(String value) {
      this.value = value;
    }

    public String getDescription() {
      return description;
    }

    @Override
    public String toString() {
      return value;
    }

    public boolean shouldShowRed(String movementReason) {
      boolean isPhysicalInventory = this == MovementType.PHYSICAL_INVENTORY
          && MovementReasonManager.INVENTORY.equalsIgnoreCase(movementReason);
      return isPhysicalInventory
          || this == MovementType.INITIAL_INVENTORY
          || this == MovementType.RECEIVE
          || (this == MovementType.ISSUE && MovementReasonManager.UNPACK_KIT.equalsIgnoreCase(movementReason));
    }
  }

  @Data
  public static class MovementReason {

    MovementType movementType;
    String code;
    String description;

    public MovementReason(MovementType type, String code, String description) {
      this.movementType = type;
      this.code = code;
      this.description = description;
    }

    protected boolean canBeDisplayOnMovementMenu() {
      return !(code.startsWith(ChangeMovementReasonToCode.DEFAULT_PREFIX)
          || code.equalsIgnoreCase(INVENTORY)
          || MovementReasonManager.UNPACK_KIT.equals(code));
    }
  }
}
