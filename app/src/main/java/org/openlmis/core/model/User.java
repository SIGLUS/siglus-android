package org.openlmis.core.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable(tableName = "users")
public class User {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField
    String username;

    @DatabaseField
    String password;

    @DatabaseField
    String facilityId;

}
