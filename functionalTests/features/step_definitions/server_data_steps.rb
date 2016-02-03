Given(/^Server updates drug data/) do
  system("cd #{LMIS_MOZ_DIR} && bash ./data/functional_tests/update_products.sh")
end

Given(/^Server updates stock_movements data/) do
  if !Dir.exists?(LMIS_MOZ_DIR)
    system("git clone https://github.com/clintonhealthaccess/lmis-moz.git #{LMIS_MOZ_DIR}")
  else
    system("cd #{LMIS_MOZ_DIR} && git checkout . && git pull -f origin master")
  end

  system("cd #{LMIS_MOZ_DIR} && bash ./data/functional_tests/regression/update_stock_movements.sh")
end

Then(/^I clean up server drug data which I updated/) do
  system("cd #{LMIS_MOZ_DIR} && bash ./data/functional_tests/rollback_updated_products.sh")
end

Given(/^server deactivates products 12D03 and 07L01/) do
  system("cd #{LMIS_MOZ_DIR} && bash ./data/functional_tests/deactivate_products.sh")
end

When(/^server reactive products/) do
  system("cd #{LMIS_MOZ_DIR} && bash ./data/functional_tests/reactivate_products.sh")
end

Given(/^server deactivates products has stock movement/) do
  system("cd #{LMIS_MOZ_DIR} && bash ./data/functional_tests/deactivate_products_have_stock_movement.sh")
end
