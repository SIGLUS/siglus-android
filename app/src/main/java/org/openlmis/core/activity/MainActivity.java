package net.gongmingqm10.androidtemplate.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import net.gongmingqm10.androidtemplate.R;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends Activity {

    @InjectView(R.id.first_number_edit_text)
    protected EditText firstNumberEdit;

    @InjectView(R.id.second_number_edit_text)
    protected EditText secondNumberEdit;

    @InjectView(R.id.result_btn)
    protected Button resultBtn;

    @InjectView(R.id.result_label)
    protected TextView resultLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.result_btn)
    protected void calculateResult() {
        Double firstNumber = Double.parseDouble(firstNumberEdit.getText().toString());
        Double secondNumber = Double.parseDouble(secondNumberEdit.getText().toString());

        if (secondNumber == 0D) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.second_number_not_zero)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            secondNumberEdit.setText("");
        } else {
            String formattedResult = new DecimalFormat("##.##").format(firstNumber / secondNumber);
            resultLabel.setText(getResources().getString(R.string.calculate_result_template, formattedResult));
        }
    }

    @OnTextChanged({R.id.first_number_edit_text, R.id.second_number_edit_text})
    protected void numberTextChanged() {
        if (TextUtils.isEmpty(firstNumberEdit.getText().toString())
                || TextUtils.isEmpty(secondNumberEdit.getText().toString())) {
            resultBtn.setEnabled(false);
        } else {
            resultBtn.setEnabled(true);
        }
    }

}
