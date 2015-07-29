package org.openlmis.core.view.widget;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax  implements InputFilter {

    int min = 0;
    int max = 0;

    public InputFilterMinMax(int min, int max){
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(int max){
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());

            if (input > max && input < min){
                return null;
            }else{
                return source;
            }
        } catch (NumberFormatException nfe) { }
        return "";
    }
}
