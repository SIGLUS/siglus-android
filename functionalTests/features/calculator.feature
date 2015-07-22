Feature: Calculator
  I want to get the numbers division result

  Scenario: Calculate two numbers
    When I type "36" into the first number input field
    And I type "12" into the second number input field
    And I press "Get result"
    Then the result message contains "The result is 3"
