package org.openlmis.core.model;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;


import java.util.Date;

import lombok.Data;


@Data
public abstract class BaseModel {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    public BaseModel(){
        createdAt = new Date();
        updatedAt = createdAt;
    }

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    private java.util.Date createdAt;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    private java.util.Date updatedAt;
}
