@requisition
Feature: Requisition

  Scenario: As a suer,I want to Requisition
    Given I am logged in
    Given I am Initialized Inventory

    And I press "Via Classica Requesition"
    Then I wait for the "RequisitionActivity" screen to appear
    Then I wait for 1 second

    Then I should see text containing "Acyclovir, tablet 400mg"
    Then I should see "5" products

    When I enter consultationsNub "888"
    Then I swipe right
    Then I swipe right
    Then I swipe right
    Then I enter QuantityRequested "345"
    And I press "Save"

    When I press view with id "btn_requisition"
    Then I swipe right
    Then I swipe right
    Then I should see "345"
    And I press "Complete"
