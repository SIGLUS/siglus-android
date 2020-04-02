package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.Date;
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

    public void save(DirtyDataItemInfo fromRule) {
        boolean updatedItem = false;
        try {
            List<DirtyDataItemInfo> itemInfos = listAll();

            for (DirtyDataItemInfo fromDB : itemInfos) {
                if (hasSavedNeedUpdate(fromDB, fromRule)) {
                    fromRule.setId(fromDB.getId());
                    fromRule.setProductCode(fromDB.getProductCode());
                    deleteItemInfoGenericDao.createOrUpdate(fromRule);
                    updatedItem = true;
                }
            }
            if (!updatedItem) {
                deleteItemInfoGenericDao.createOrUpdate(fromRule);
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    private boolean hasSavedNeedUpdate(DirtyDataItemInfo fromDB, DirtyDataItemInfo fromRule) {
        return fromDB.getProductCode().equals(fromRule.getProductCode());
    }

    private List<DirtyDataItemInfo> listAll() {
        try {
            return deleteItemInfoGenericDao.queryForAll();
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DirtyDataItemInfo> listunSyced() {
        try {
            return FluentIterable.from(deleteItemInfoGenericDao.queryForAll())
                    .filter(dirtyDataItemInfo -> !dirtyDataItemInfo.isSynced()).toList();
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createOrUpdateWithItem(DirtyDataItemInfo itemInfo) {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
                    () -> {
                        deleteItemInfoGenericDao.refresh(itemInfo);
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasBackedData(List<DirtyDataItemInfo> infos) {
        return infos != null && infos.size() > 0;
    }

    public boolean hasOldDate() {
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(new Date(), 1);
        List<DirtyDataItemInfo> infos = listAll();
        if (hasBackedData(infos)) {
            for (DirtyDataItemInfo itemInfo : infos) {
                if (itemInfo.getCreatedAt().before(dueDateShouldDataLivedInDB)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteOldData() {
        Date dueDateShouldDataLivedInDB = DateUtil.dateMinusMonth(new Date(), 1);
        try {
            dbUtil.withDao(DirtyDataItemInfo.class, dao -> {
                DeleteBuilder<DirtyDataItemInfo, String> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().le("createdAt", dueDateShouldDataLivedInDB);
                deleteBuilder.delete();
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
