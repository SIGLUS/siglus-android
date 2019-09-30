package org.openlmis.core.model.builder;

import org.openlmis.core.model.User;


public class UserBuilder {
    private User user;

    public UserBuilder() {
        user = new User();
    }

    public UserBuilder setUsername(String username) {
        user.setUsername(username);
        return this;
    }

    public UserBuilder setPassword(String password) {
        user.setPassword(password);
        return this;
    }

    public UserBuilder setFacilityName(String facilityName) {
        user.setFacilityName(facilityName);
        return this;
    }

    public UserBuilder setFacilityCode(String facilityCode) {
        user.setFacilityName(facilityCode);
        return this;
    }

    public User build() {
        return user;
    }

    public static User defaultUser() {
        return new UserBuilder().setUsername("username")
                .setPassword("password")
                .setFacilityName("Facility Name")
                .setFacilityCode("facility_code")
                .build();
    }
}
