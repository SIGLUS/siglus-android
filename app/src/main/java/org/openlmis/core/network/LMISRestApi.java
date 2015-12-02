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


package org.openlmis.core.network;

import com.google.gson.JsonObject;

import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.model.ProductsResponse;
import org.openlmis.core.network.model.StockCardResponse;
import org.openlmis.core.network.model.SubmitRequisitionResponse;
import org.openlmis.core.network.model.SyncBackRequisitionsResponse;
import org.openlmis.core.network.model.StockMovementEntry;

import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface LMISRestApi {

    @POST("/rest-api/login")
    void authorizeUser(@Body User user, Callback<UserRepository.UserResponse> callback);

    @GET("/rest-api/programs-with-products")
    ProductsResponse fetchProducts(@Query("facilityCode") String facilityCode);

    @GET("/rest-api/requisitions")
    SyncBackRequisitionsResponse fetchRequisitions(@Query("facilityCode") String facilityCode);

    @POST("/rest-api/requisitions")
    SubmitRequisitionResponse submitRequisition(@Body RnRForm rnRForm);

    @POST("/rest-api/facilities/{facilityId}/stockCards")
    JsonObject pushStockMovementData(@Path("facilityId") String facilityId, @Body List<StockMovementEntry> entries);

    @GET("/rest-api/facilities/{facilityId}/stockCards")
    StockCardResponse fetchStockMovementData(@Path("facilityId") String facilityId, @Query("startTime") Date start, @Query("endTime") Date end);
}
