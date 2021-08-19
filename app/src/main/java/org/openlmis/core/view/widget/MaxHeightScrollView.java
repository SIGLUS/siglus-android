package org.openlmis.core.view.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.widget.ScrollView;
import androidx.annotation.RequiresApi;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public class MaxHeightScrollView extends ScrollView {

  private int maxHeight;

  public MaxHeightScrollView(Context context) {
    super(context);
  }

  public MaxHeightScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public MaxHeightScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    if (attrs != null) {
      TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView);
      maxHeight = (int) styledAttrs.getDimension(R.styleable.MaxHeightScrollView_scrollViewMaxHeight,
          LMISApp.getContext().getResources().getDimension(R.dimen.px_180));
      styledAttrs.recycle();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

}
