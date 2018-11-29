package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.text.DateFormat;
import java.util.Date;

public class ReportTypeFormAdapter {
    private final Gson gson;

    public ReportTypeFormAdapter() {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
                .create();
    }
}
