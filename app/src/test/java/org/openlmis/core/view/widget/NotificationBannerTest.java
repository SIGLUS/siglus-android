package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;

@RunWith(LMISTestRunner.class)
public class NotificationBannerTest {

  @Test
  public void shouldSetText() {
    String notification = "notification";
    // when
    NotificationBanner notificationBanner = new NotificationBanner(LMISApp.getContext());
    notificationBanner.setNotificationMessage(notification);
    // then
    assertEquals(notification, notificationBanner.txMissedRequisition.getText());
  }
}