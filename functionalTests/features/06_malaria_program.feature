@malaria ~@ignore
Feature: Add values to malaria program chart for US and APE and Save Information

  Scenario: 01 - Save information on Patient data malaria program
    Given today is "20160218.130000"
    And I log in into the application
    And I press "VIA PATIENT DATA"
    And I should see text containing "No patient data report has been created"
    And I press "Create patient data report"
    And I enter values for Malaria program report
    When I save malaria program report
    Then I wait up to 30 seconds to see "Successfully Saved"
