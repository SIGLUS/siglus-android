@regression @1s
Feature: low stock

  Scenario: Calculate low stock and cmm without stock out in 3 months
  Given I change device date to "20160216.130000"
  Given I try to log in with "superuser" "password1"
  And I wait up to 180 seconds for "Initial Inventory" to appear

  When I search lot product by primary name "Digoxina 0,25mg Comp" and select this item with quantity "500" and lot number "dkdk"
  And I wait for "Complete" to appear
  And I press "Complete"
  Then I wait for "STOCK CARD OVERVIEW" to appear
  And I press "Stock Card Overview"
  Then I wait for "Stock Overview" to appear
  And I make a new movement "01A01" "Issues" "PAV" "100"
  Then I should see CMM ""
  Given I change device date to "20160316.130000"
  And I navigate back

  Given I try to log in with "superuser" "password1"
  Then I wait for "STOCK CARD OVERVIEW" to appear
  And I press "Stock Card Overview"
  Then I wait for "Stock Overview" to appear
  Then I wait for 1 second
  And I make a new movement "01A01" "Issues" "PAV" "123"
  Then I should see CMM ""
  Given I change device date to "20160416.130000"
  And I navigate back

  Given I try to log in with "superuser" "password1"
  Then I wait for "STOCK CARD OVERVIEW" to appear
  And I press "Stock Card Overview"
  Then I wait for "Stock Overview" to appear
  Then I wait for 1 second
  And I make a new movement "01A01" "Issues" "PAV" "210"
  Then I should see CMM ""
  Given I change device date to "20160516.130000"
  And I navigate back

  Given I try to log in with "superuser" "password1"
  Then I wait for "STOCK CARD OVERVIEW" to appear
  When I press "Stock Card Overview"
  And I wait for "Stock Overview" to appear
  Then I select stock card code called "01A01"
  Then I wait for "Stock Card" to appear
  Then I should see CMM "144.33"
