package org.openlmis.core.manager;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.User;

@RunWith(LMISTestRunner.class)
public class UserInfoMgrTest {

  private UserInfoMgr userInfoMgr = UserInfoMgr.getInstance();

  @Test
  public void shouldReturnEmptyStringWhenGetProvinceNameIsCalledAndUserIsNull() {
    // given
    setUserToNull();
    // when
    String actualProvinceName = userInfoMgr.getProvinceName();
    // then
    assertEquals("", actualProvinceName);
  }

  @Test
  public void shouldReturnProvinceNameWhenGetProvinceNameIsCalledAndUserIsNotNull() {
    // given
    String provinceName = "provinceName";

    User user = new User("", "");
    user.setProvinceName(provinceName);
    userInfoMgr.setUser(user);
    // when
    String actualProvinceName = userInfoMgr.getProvinceName();
    // then
    assertEquals(provinceName, actualProvinceName);
  }

  @Test
  public void shouldReturnEmptyStringWhenGetDistrictNameIsCalledAndUserIsNull() {
    // given
    setUserToNull();
    // when
    String actualDistrictName = userInfoMgr.getDistrictName();
    // then
    assertEquals("", actualDistrictName);
  }

  @Test
  public void shouldReturnDistrictNameWhenGetDistrictNameIsCalledAndUserIsNotNull() {
    // given
    String districtName = "districtName";

    User user = new User("", "");
    user.setDistrictName(districtName);
    userInfoMgr.setUser(user);
    // when
    String actualDistrictName = userInfoMgr.getDistrictName();
    // then
    assertEquals(districtName, actualDistrictName);
  }

  private void setUserToNull() {
    try {
      userInfoMgr.setUser(null);
    } catch (NullPointerException e) {

    }
  }
}