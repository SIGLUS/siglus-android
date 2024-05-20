package org.openlmis.core.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.openlmis.core.R;

public class ExpiredProductsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expired_products);
    }
}