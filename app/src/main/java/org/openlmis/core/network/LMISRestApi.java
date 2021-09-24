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

import java.util.List;
import org.openlmis.core.annotation.DeleteWithBody;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.network.model.AppInfoRequest;
import org.openlmis.core.network.model.CmmEntry;
import org.openlmis.core.network.model.DirtyDataItemEntry;
import org.openlmis.core.network.model.FacilityInfoResponse;
import org.openlmis.core.network.model.PodEntry;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownRegimensResponse;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;
import org.openlmis.core.network.model.SyncUpDeletedMovementResponse;
import org.openlmis.core.network.model.SyncUpRequisitionResponse;
import org.openlmis.core.network.model.SyncUpStockMovementDataSplitResponse;
import org.openlmis.core.network.model.UserResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Query;

@SuppressWarnings("PMD")
public interface LMISRestApi {

  @GET("/api/siglusapi/android/me/facility/products")
  SyncDownLatestProductsResponse fetchLatestProducts(@Query("lastSyncTime") String afterUpdatedTime)
      throws LMISException;

  @GET("/api/siglusapi/android/regimens")
  SyncDownRegimensResponse fetchRegimens() throws LMISException;

  @GET("/api/siglusapi/android/me/facility")
  FacilityInfoResponse fetchFacilityInfo() throws LMISException;

  @GET("/api/siglusapi/android/me/facility/stockCards")
  StockCardsLocalResponse fetchStockMovementData(@Query("startTime") String startDate, @Query("endTime") String endDate)
      throws LMISException;

  @GET("/api/siglusapi/android/me/facility/requisitions")
  SyncDownRequisitionsResponse fetchRequisitions(@Query("startDate") String startDate) throws LMISException;

  @GET("/api/siglusapi/android/me/facility/pods")
  List<Pod> fetchPods(@Query("shippedOnly") boolean shippedOnly) throws LMISException;

  @PATCH("/api/siglusapi/android/me/facility/pod")
  Pod submitPod(@Body PodEntry podEntry) throws LMISException;

  @POST("/api/oauth/token")
  void login(@Query("grant_type") String grantType, @Query("username") String username,
      @Query("password") String password, Callback<UserResponse> callback);

  @POST("/api/siglusapi/android/me/facility/archivedProducts")
  Void syncUpArchivedProducts(@Body List<String> archivedProductsCodes) throws LMISException;

  @POST("/api/siglusapi/android/me/app-info")
  Void updateAppVersion(@Body AppInfoRequest appInfo) throws LMISException;

  @POST("/api/siglusapi/android/me/facility/requisitions")
  Void submitRequisition(@Body RnRForm rnRForm) throws LMISException;

  @POST("/api/siglusapi/android/me/facility/requisitions")
  SyncUpRequisitionResponse submitEmergencyRequisition(@Body RnRForm rnRForm) throws LMISException;

  @POST("/api/siglusapi/android/me/facility/stockCards")
  SyncUpStockMovementDataSplitResponse syncUpStockMovementDataSplit(@Body List<StockMovementEntry> entries)
      throws LMISException;

  @DeleteWithBody("/api/siglusapi/android/me/facility/stockCards")
  SyncUpDeletedMovementResponse syncUpDeletedData(@Body List<DirtyDataItemEntry> entryList) throws LMISException;

  @POST("/api/siglusapi/android/me/facility/cmms")
  Void syncUpCmms(@Body List<CmmEntry> cmms) throws LMISException;

  // below are v2 api
  @GET("/rest-api/re-sync")
  Void recordReSyncAction() throws LMISException;

}
