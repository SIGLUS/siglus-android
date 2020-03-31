package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;


public class DirtyDataRepository {

    private static final String TAG = DirtyDataRepository.class.getSimpleName();

    GenericDao<DirtyDataItemInfo> deleteItemInfoGenericDao;
    private Context context;

    @Inject
    DbUtil dbUtil;

    @Inject
    public DirtyDataRepository(Context context) {
        deleteItemInfoGenericDao = new GenericDao<>(DirtyDataItemInfo.class, context);
        this.context = context;
    }

    public void save(DirtyDataItemInfo itemInfo) {
        try {
            dbUtil.withDaoAsBatch(DirtyDataItemInfo.class, dao -> dao.createOrUpdate(itemInfo));
        } catch (LMISException e) {
            e.printStackTrace();
        }
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

}
