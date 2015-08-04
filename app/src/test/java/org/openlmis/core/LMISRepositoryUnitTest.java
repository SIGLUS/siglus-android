package org.openlmis.core;


import org.junit.After;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public abstract class LMISRepositoryUnitTest {

    @After
    public void tearDown() throws Exception{
        LmisSqliteOpenHelper.closeHelper();
    }
}
