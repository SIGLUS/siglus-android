package org.openlmis.core.view.widget;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.ArrayList;

public class CleanableEditText extends androidx.appcompat.widget.AppCompatEditText {
    private ArrayList<TextWatcher> mListeners = null;

    public CleanableEditText(Context ctx) {
        super(ctx);
    }

    public CleanableEditText(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public CleanableEditText(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(watcher);
        super.addTextChangedListener(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (mListeners != null) {
            int i = mListeners.indexOf(watcher);
            if (i >= 0) {
                mListeners.remove(i);
            }
        }
        super.removeTextChangedListener(watcher);
    }

    public void clearTextChangedListeners() {
        if (mListeners != null) {
            for (TextWatcher watcher : mListeners) {
                super.removeTextChangedListener(watcher);
            }
            mListeners.clear();
            mListeners = null;
        }
    }
}
