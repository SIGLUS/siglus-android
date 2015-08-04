package org.openlmis.core.model.repository;


import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.MIMIAForm;
import org.openlmis.core.model.MIMIAProductItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MIMIARepository {
    @Inject
    DbUtil dbUtil;

    @Inject
    StockRepository stockRepository;

    @Inject
    RegimenRepository regimenRepository;

    GenericDao<MIMIAForm> genericDao;

    @Inject
    public MIMIARepository(Context context) {
        this.genericDao = new GenericDao<>(MIMIAForm.class, context);
    }

    public void create(MIMIAForm form) throws LMISException {
        genericDao.create(form);
    }

    public List<MIMIAForm> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public void save(MIMIAForm form) throws LMISException {
        genericDao.update(form);
    }

    public void createProductItems(final List<MIMIAProductItem> productItems) throws LMISException {
        dbUtil.withDaoAsBatch(MIMIAProductItem.class, new DbUtil.Operation<MIMIAProductItem, Void>() {
            @Override
            public Void operate(Dao<MIMIAProductItem, String> dao) throws SQLException {
                for (MIMIAProductItem item : productItems) {
                    dao.create(item);
                }
                return null;
            }
        });
    }


    public void createRegimenItems(final List<RegimenItem> regimenItemList) throws LMISException {
        dbUtil.withDaoAsBatch(RegimenItem.class, new DbUtil.Operation<RegimenItem, Void>() {
            @Override
            public Void operate(Dao<RegimenItem, String> dao) throws SQLException {
                for (RegimenItem item : regimenItemList) {
                    dao.create(item);
                }
                return null;
            }
        });
    }


    public MIMIAForm initMIMIA() throws LMISException {

        MIMIAForm form = new MIMIAForm();
        create(form);
        createProductItems(generateProductItems(form));
        createRegimenItems(generateRegimeItems(form));
        return form;
    }

    private List<RegimenItem> generateRegimeItems(MIMIAForm form) throws LMISException {
        List<Regimen> regimens = regimenRepository.list();
        List<RegimenItem> regimenItems = new ArrayList<>();
        for (Regimen regimen : regimens) {
            RegimenItem item = new RegimenItem();
            item.setForm(form);
            item.setRegimen(regimen);
            regimenItems.add(item);
        }
        return regimenItems;
    }

    private List<MIMIAProductItem> generateProductItems(MIMIAForm form) throws LMISException {
        List<StockCard> stockCards = stockRepository.list("ART");
        List<MIMIAProductItem> productItems = new ArrayList<>();

        Calendar calendar = GregorianCalendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        Date startDate = new GregorianCalendar(calendar.get(Calendar.YEAR), month - 1, 20).getTime();
        Date endDate = new GregorianCalendar(calendar.get(Calendar.YEAR), month, 20).getTime();

        for (StockCard stockCard : stockCards) {
            List<StockItem> stockItems = stockRepository.queryStockItems(stockCard, startDate, endDate);
            if (stockItems.size() > 0) {
                MIMIAProductItem productItem = new MIMIAProductItem();

                StockItem firstItem = stockItems.get(0);
                productItem.setInitialAmount(firstItem.getStockOnHand() + firstItem.getAmount());

                productItem.setReceived(stockRepository
                        .sum(StockRepository.MOVEMENTTYPE.RECEIVE.toString(), stockCard, startDate, endDate));
                productItem.setIssued(stockRepository
                        .sum(StockRepository.MOVEMENTTYPE.ISSUE.toString(), stockCard, startDate, endDate));
                productItem.setAdjustment(stockRepository.sum(StockRepository.MOVEMENTTYPE.POSADJUST.toString(), stockCard, startDate, endDate)
                        - stockRepository.sum(StockRepository.MOVEMENTTYPE.NEGADJUST.toString(), stockCard, startDate, endDate));
                productItem.setForm(form);

                productItems.add(productItem);
            }
        }

        return productItems;
    }
}
