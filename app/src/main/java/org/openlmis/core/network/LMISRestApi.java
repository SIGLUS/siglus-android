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

import org.json.JSONObject;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.User;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.CmmEntry;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownReportTypeResponse;
import org.openlmis.core.network.model.SyncDownProgramDataResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncDownStockCardResponse;
import org.openlmis.core.network.model.SyncUpRequisitionResponse;
import org.openlmis.core.network.model.UserResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface LMISRestApi {

    @POST("/rest-api/login")
    void authorizeUser(@Body User user, Callback<UserResponse> callback);


    //sync up

    @POST("/rest-api/requisitions")
    SyncUpRequisitionResponse submitRequisition(@Body RnRForm rnRForm) throws LMISException;

    @POST("/rest-api/sdp-requisitions")
    SyncUpRequisitionResponse submitEmergencyRequisition(@Body RnRForm rnRForm) throws LMISException;

    @POST("/rest-api/facilities/{facilityId}/stockCards")
    JSONObject syncUpStockMovementData(@Path("facilityId") String facilityId, @Body List<StockMovementEntry> entries) throws LMISException;

    @POST("/rest-api/facilities/{facilityId}/unSyncedStockCards")
    Void syncUpUnSyncedStockCards(@Path("facilityId") String facilityId, @Body List<String> unSyncedStockCardCodes) throws LMISException;

    @POST("/rest-api/facilities/{facilityId}/archivedProducts")
    Void syncUpArchivedProducts(@Path("facilityId") String facilityId, @Body List<String> archivedProductsCodes) throws LMISException;

    @PUT("/rest-api/facilities/{facilityId}/Cmms")
    Void syncUpCmms(@Path("facilityId") String facilityId, @Body List<CmmEntry> cmms) throws LMISException;

    @POST("/rest-api/update-app-info")
    Void updateAppVersion(@Body AppInfoRequest appInfo) throws LMISException;


    //sync down

    @GET("/rest-api/requisitions")
    SyncDownRequisitionsResponse fetchRequisitions(@Query("facilityCode") String facilityCode) throws LMISException;

    @GET("/rest-api/facilities/{facilityId}/stockCards")
    SyncDownStockCardResponse fetchStockMovementData(@Path("facilityId") String facilityId, @Query("startTime") String startDate, @Query("endTime") String endDate) throws LMISException;

    @GET("/rest-api/latest-products")
    SyncDownLatestProductsResponse fetchLatestProducts(@Query("afterUpdatedTime") String afterUpdatedTime) throws LMISException;

    @GET("/rest-api/programData/facilities/{facilityId}")
    SyncDownProgramDataResponse fetchProgramDataForms(@Path("facilityId") Long facilityId) throws LMISException;

    @GET("/rest-api/report-types/mapping/{facilityId}")
    SyncDownReportTypeResponse fetchReportTypeForms(@Path("facilityId") Long facilityId) throws LMISException;

    @POST("/rest-api/programData")
    Void syncUpProgramDataForm(@Body ProgramDataForm programDataForm) throws LMISException;

    Void syncUpMalariaPrograms(List<MalariaProgram> malariaPrograms) throws LMISException;
}
