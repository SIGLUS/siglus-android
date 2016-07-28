package org.openlmis.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;

public class AddedDrugInVIA implements Parcelable{

    @Getter
    private String productCode;

    @Getter
    private long quantity;

    public AddedDrugInVIA(String productCode, long quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public static final Creator<AddedDrugInVIA> CREATOR = new Creator<AddedDrugInVIA>() {
        @Override
        public AddedDrugInVIA createFromParcel(Parcel in) {
            return new AddedDrugInVIA(in.readString(), in.readLong());
        }

        @Override
        public AddedDrugInVIA[] newArray(int size) {
            return new AddedDrugInVIA[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productCode);
        dest.writeLong(quantity);
    }
}
