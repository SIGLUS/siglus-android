Given(/^Server updates drug data/) do
  system("cd #{LMIS_MOZ_DIR} && ./data/functional_tests/update_products.sh")
end

Then(/^I clean up server drug data which I updated/) do
  system("cd #{LMIS_MOZ_DIR} && ./data/functional_tests/rollback_updated_products.sh")
end

Given(/^server deactivates products 12D03 and 07L01/) do
  system("cd #{LMIS_MOZ_DIR} && ./data/functional_tests/deactivate_products.sh")
end