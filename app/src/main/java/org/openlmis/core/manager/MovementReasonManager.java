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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.model.StockMovementItem;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Data;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Singleton
public class MovementReasonManager {

    Context context;
    public static final String RES_DIVIDER = "[|]";

    List<MovementReason> reasonListEN;
    List<MovementReason> reasonListPT;
    List<MovementReason> fullList;

    List<MovementReason> currentReasonList;

    @Inject
    public MovementReasonManager(Context context){
        this.context = context;
        reasonListEN = new ArrayList<>();
        reasonListPT = new ArrayList<>();

        initReasonList();
    }

    private void initReasonList() {
        initReasonList(getResourceByLocal(new Locale("pt", "pt")).getStringArray(R.array.reason_data), reasonListPT);
        initReasonList(getResourceByLocal(Locale.ENGLISH).getStringArray(R.array.reason_data), reasonListEN);

        if (context.getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("pt")){
            currentReasonList = newArrayList(reasonListPT);
        }else {
            currentReasonList = newArrayList(reasonListEN);
        }

        fullList = newArrayList(reasonListEN);
        fullList.addAll(reasonListPT);
    }

    private void initReasonList(String[] reasonList, List<MovementReason> fullList) {
        for (String s : reasonList) {
            String[] values = s.split(RES_DIVIDER);

            if (values.length < 3) {
                Log.d(getClass().getSimpleName(), "Invalid Config =>" + s);
                continue;
            }

            MovementReason reason = new MovementReason(StockMovementItem.MovementType.valueOf(values[0]), values[1], values[2]);
            fullList.add(reason);
        }
    }


    public List<MovementReason> buildReasonListForMovementType(final StockMovementItem.MovementType type){
        return FluentIterable.from(currentReasonList).filter(new Predicate<MovementReason>() {
            @Override
            public boolean apply(MovementReason movementReason) {
                return movementReason.getMovementType() == type && canBeDisplayOnMovementMenu(movementReason.getCode());
            }
        }).toList();
    }

    protected boolean canBeDisplayOnMovementMenu(String code){
        return !(code.startsWith("DEFAULT") || code.equalsIgnoreCase("INVENTORY"));
    }

    public String queryForCode(final String reason) throws MovementReasonNotFoundException{
        if (StringUtils.isEmpty(reason)){
            return StringUtils.EMPTY;
        }

        Optional<MovementReason> matched = FluentIterable.from(fullList).firstMatch(new Predicate<MovementReason>() {
            @Override
            public boolean apply(MovementReason movementReason) {
                return reason.equalsIgnoreCase(movementReason.getDescription());
            }
        });


        if (!matched.isPresent()){
            throw new MovementReasonNotFoundException(reason);
        }

        return matched.get().getCode();
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
    }
}
