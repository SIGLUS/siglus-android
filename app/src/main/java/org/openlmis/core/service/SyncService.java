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

package org.openlmis.core.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.User;
import org.openlmis.core.training.TrainingSyncAdapter;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.List;

import roboguice.inject.InjectResource;

import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.cancelSync;
import static android.content.ContentResolver.requestSync;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class SyncService extends Service {
    private static final Object SYNC_ADAPTER_LOCK = new Object();
    private SyncAdapter syncAdapter = null;
    private final String tag = "SyncService";

    @Inject
    private AccountManager accountManager;
    @Inject
    private TrainingSyncAdapter trainingSyncAdapter;
    @InjectResource(R.string.sync_content_authority)
    private String syncContentAuthority;
    @InjectResource(R.string.sync_account_type)
    private String syncAccountType;
    @InjectResource(R.integer.sync_interval)
    private Integer syncInterval;

    @Override
    public void onCreate() {
        synchronized (SYNC_ADAPTER_LOCK) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }

    public void createSyncAccount(User user) {
        Account account = new Account(user.getUsername(), syncAccountType);
        accountManager.addAccountExplicitly(account, user.getPassword(), null);
        Log.d(tag, "sync account created");
    }

    public void kickOff() {
        Account account = findFirstLmisAccount();
        if (account != null) {
            setIsSyncable(account, syncContentAuthority, 1);
            setSyncAutomatically(account, syncContentAuthority, true);
            addPeriodicSync(account, syncContentAuthority, periodicSyncParams(), syncInterval);
        }
        Log.d(tag, "sync service kicked off");
    }

    public void requestSyncImmediatelyFromUserTrigger() {
        requestSyncImmediately(true);
    }

    public void requestSyncImmediatelyByTask() {
        requestSyncImmediately(false);
    }

    private void requestSyncImmediately(boolean isUserTriggered) {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            trainingSyncAdapter.requestSync();
        } else {
            Log.d(tag, "immediate sync up requested");
            Account account = findFirstLmisAccount();
            if (account != null) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                bundle.putBoolean("isUserTriggered", isUserTriggered);

                requestSync(account, syncContentAuthority, bundle);
            }
        }
    }

    public void shutDown() {
        Account account = findFirstLmisAccount();
        if (account != null) {
            cancelSync(account, syncContentAuthority);
            setSyncAutomatically(account, syncContentAuthority, false);
        }
        Log.d(tag, "sync service stopped");
    }

    private Bundle periodicSyncParams() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        return extras;
    }

    private Account findFirstLmisAccount() {
        List<Account> accounts = newArrayList(accountManager.getAccounts());
        List<Account> lmisAccounts = from(accounts).filter(new Predicate<Account>() {
            @Override
            public boolean apply(Account input) {
                return syncAccountType.equals(input.type);
            }
        }).toList();

        if (lmisAccounts.size() > 0) {
            return lmisAccounts.get(0);
        }

        return null;
    }
}
