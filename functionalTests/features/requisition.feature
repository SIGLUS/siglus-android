@Requisition
Feature: Requisition

  Scenario: Go to requisition page and display all VIA products
    Given I am logged in
    Given I am Initialized Inventory

    And I press "Via Classical Requisition"
    Then I wait for the "RequisitionActivity" screen to appear
    Then I should see text containing "Acyclovir, tablet 400mg"
    Then I should see "5" products

  Scenario:Pop up alert
    Given I am logged in
    And I press "Via Classical Requisition"
    Then I wait for the "RequisitionActivity" screen to appear
    When I enter consultationsNub "111"
    Then I go back
    Then I wait to see "Are you sure you want to quit without saving your work?"
    Then I press "Yes"
    Then I wait for the "RequisitionActivity" screen to appear

  Scenario: Save draft and complete
    Given I am logged in
    And I press "Via Classical Requisition"
    When I enter consultationsNub "888"
    Then I swipe right
    Then I swipe right
    Then I swipe right
    Then I enter QuantityRequested "345"
    And I press "Save"
    Then I wait for the "RequisitionActivity" screen to appear

    When I press view with id "btn_requisition"
    Then I swipe right
    Then I swipe right
    Then I should see "345"
    Then I go back
    Then I press "Complete"
    Then I wait for the "RequisitionActivity" screen to appear
    


