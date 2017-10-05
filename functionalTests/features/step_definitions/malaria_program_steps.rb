def malaria_program_page
  @malaria_program_page ||= page(MalariaProgramPage).await(timeout: 30)
end

Given(/^I enter values for Malaria program report$/) do
  malaria_program_page.set_values
end

Given(/^I save malaria program report$/) do
  malaria_program_page.save_patient_data_report
end
