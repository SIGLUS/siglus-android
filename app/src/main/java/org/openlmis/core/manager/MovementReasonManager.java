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
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.model.StockMovementItem;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;



@Singleton
public final class MovementReasonManager {

    public static final String INVENTORY_POSITIVE = "INVENTORY_POSITIVE";
    public static final String INVENTORY_NEGATIVE = "INVENTORY_NEGATIVE";
    public static final String DEFAULT = "DEFAULT";
    public static final String INVENTORY = "INVENTORY";

    public static final String DEFAULT_ISSUE = "DEFAULT_ISSUE";
    public static final String DEFAULT_RECEIVE = "DEFAULT_RECEIVE";
    public static final String DEFAULT_NEGATIVE_ADJUSTMENT = "DEFAULT_NEGATIVE_ADJUSTMENT";
    public static final String DEFAULT_POSITIVE_ADJUSTMENT = "DEFAULT_POSITIVE_ADJUSTMENT";

    Context context;
    public static final String RES_DIVIDER = "[|]";

    List<MovementReason> currentReasonList;
    private static MovementReasonManager instance;


    Map<String, ArrayList<MovementReason>> reasonCache;

    @Inject
    private MovementReasonManager(Context context){
        this.context = context;
        reasonCache = new HashMap<>();
        currentReasonList = initReasonList(this.context.getResources().getConfiguration().locale);
    }

    public static MovementReasonManager getInstance(){
        if (instance == null){
            instance = new MovementReasonManager(LMISApp.getContext());
        }
        return instance;
    }


    public void refresh(){
        instance = new MovementReasonManager(LMISApp.getContext());
    }

    private ArrayList<MovementReason> initReasonList(Locale locale) {
        if (reasonCache.containsKey(locale.getLanguage())){
            return reasonCache.get(locale.getLanguage());
        }

        String[] reasonData = getResourceByLocal(locale).getStringArray(R.array.reason_data);
        ArrayList<MovementReason> reasonList = parseReasonListFromConfig(reasonData);
        reasonCache.put(locale.getLanguage(), reasonList);
        return reasonList;
    }

    @NonNull
    private static ArrayList<MovementReason>  parseReasonListFromConfig(String[] reasonData) {
        ArrayList<MovementReason>  reasonList = new ArrayList<>();
        for (String s : reasonData) {
            String[] values = s.split(RES_DIVIDER);

            if (values.length < 3) {
                Log.d(MovementReasonManager.class.getSimpleName(), "Invalid Config =>" + s);
                continue;
            }

            MovementReason reason = new MovementReason(StockMovementItem.MovementType.valueOf(values[0]), values[1], values[2]);
            reasonList.add(reason);
        }
        return reasonList;
    }


    public List<MovementReason> buildReasonListForMovementType(final StockMovementItem.MovementType type){
        return FluentIterable.from(currentReasonList).filter(new Predicate<MovementReason>() {
            @Override
            public boolean apply(MovementReason movementReason) {
                return movementReason.getMovementType() == type && movementReason.canBeDisplayOnMovementMenu();
            }
        }).toList();
    }

    public MovementReason queryByDesc(final String reason) throws MovementReasonNotFoundException{
        return queryByDesc(reason, context.getResources().getConfiguration().locale);
    }

    public MovementReason queryByDesc(final String desc, Locale locale) throws MovementReasonNotFoundException{
        ArrayList<MovementReason> reasonList = initReasonList(locale);

        Optional<MovementReason> matched = FluentIterable.from(reasonList).firstMatch(new Predicate<MovementReason>() {
            @Override
            public boolean apply(MovementReason movementReason) {
                return movementReason.getDescription().equalsIgnoreCase(desc);
            }
        });

        if (!matched.isPresent()){
            throw new MovementReasonNotFoundException(desc);
        }
        return matched.get();
    }


    public MovementReason queryByCode(final String code) throws MovementReasonNotFoundException{
        Optional<MovementReason> matched = FluentIterable.from(currentReasonList).firstMatch(new Predicate<MovementReason>() {
            @Override
            public boolean apply(MovementReason movementReason) {
                return movementReason.getCode().equalsIgnoreCase(code);
            }
        });

        if (!matched.isPresent()){
            throw new MovementReasonNotFoundException(code);
        }
        return matched.get();
    }

    public Resources getResourceByLocal(Locale locale){
        Resources standardResources = context.getResources();
        AssetManager assets = standardResources.getAssets();
        DisplayMetrics metrics = standardResources.getDisplayMetrics();
        Configuration config = new Configuration(standardResources.getConfiguration());
        config.locale = locale;
        return new Resources(assets, metrics, config);
    }


    @Data
    public static class MovementReason {
        StockMovementItem.MovementType movementType;
        String code;
        String description;

        public MovementReason(StockMovementItem.MovementType type, String code, String description) {
            this.movementType = type;
            this.code = code;
            this.description = description;
        }

        public boolean isInventoryAdjustment(){
            return INVENTORY_NEGATIVE.equalsIgnoreCase(code) || INVENTORY_POSITIVE.equalsIgnoreCase(code);
        }

        protected boolean canBeDisplayOnMovementMenu(){
            return !(code.startsWith(DEFAULT) || code.equalsIgnoreCase(INVENTORY));
        }
    }
}
