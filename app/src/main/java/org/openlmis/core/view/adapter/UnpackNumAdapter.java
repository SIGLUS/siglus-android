package org.openlmis.core.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import org.openlmis.core.view.widget.SelectUnpackNumCardView;

public class UnpackNumAdapter extends BaseAdapter {

  private final long kitSOH;
  private final String kitType;
  private final Context context;

  public UnpackNumAdapter(Context context, long kitSOH, String kitType) {
    this.context = context;
    this.kitSOH = kitSOH;
    this.kitType = kitType;
  }

  @Override
  public int getCount() {
    return (int) kitSOH;
  }

  @Override
  public Integer getItem(int position) {
    return position + 1;
  }

  @Override
  public long getItemId(int position) {
    return position + 1L;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    SelectUnpackNumCardView cardView;

    if (convertView == null) {
      cardView = new SelectUnpackNumCardView(context);
    } else {
      cardView = (SelectUnpackNumCardView) convertView;
    }

    cardView.populate(getItem(position), kitType);
    return cardView;
  }
}
