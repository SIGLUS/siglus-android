package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

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
}
