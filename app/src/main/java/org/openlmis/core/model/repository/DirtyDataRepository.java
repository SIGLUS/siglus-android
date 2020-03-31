package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.util.List;

public class DirtyDataRepository {

    private static final String TAG = DirtyDataRepository.class.getSimpleName();

    GenericDao<DirtyDataItemInfo> deleteItemInfoGenericDao;
    private Context context;

    @Inject
    DbUtil dbUtil;
    @Inject
    StockRepository stockRepository;
    @Inject
    RnrFormRepository rnrFormRepository;
    @Inject
    ProgramRepository programRepository;

    @Inject
    public DirtyDataRepository(Context context) {
        deleteItemInfoGenericDao = new GenericDao<>(DirtyDataItemInfo.class, context);
        this.context = context;
    }

    public void save(List<DirtyDataItemInfo> dirtyDataItemInfoList) {
        try {
            dbUtil.withDaoAsBatch(DirtyDataItemInfo.class, dao -> {
                for (DirtyDataItemInfo itemInfo : dirtyDataItemInfoList) {
                    dao.createOrUpdate(itemInfo);
                }
                return null;
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public void deleteDirtyDataByProductCode(List<String> productCodeList) {
        String deleteDraftLotItems = "DELETE FROM draft_lot_items";
        String deleteDraftInventory = "DELETE FROM draft_inventory";
        stockRepository.deleteStockDirtyData(productCodeList);
        rnrFormRepository.deleteRnrFormDirtyData(productCodeList);
        programRepository.deleteProgramDirtyData(productCodeList);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteDraftLotItems);
        LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(deleteDraftInventory);
    }
}
