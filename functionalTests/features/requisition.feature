Feature: Requisition

    Scenario: Navigate to Home Page
            Given I am logged in
            Given I am Initialized Inventory

  Scenario: Go to Requisition Page
    Given I am logged in
    And I press view with id "btn_requisition"
    Then I wait for the "RequisitionActivity" screen to appear

    Then I should see text containing "Acyclovir,tablet400mg"
    Then I should see 5 products


    When I enter consultationsNub "001"
    Then I enter QuantityRequested "100"
    Then I press "SAVE"



    When I press view with id "btn_requisition"
    Then I press "COMPLETE"
