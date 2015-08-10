Feature: Login

  Scenario: Log in to OpenLMIS
    Given I see "CHAI LMIS"
    When I enter username "superuser"
    And I enter password "password1"
    And I press "LOG IN"
    Then I should see "DONE"