require 'calabash-android/management/app_installation'

LMIS_MOZ_DIR="#{Dir.pwd}/../lmis-moz"

AfterConfiguration do |config|
  FeatureNameMemory.feature_name = nil
end

Before('@upgrade') do |scenario|
  first_time = true

  if first_time
    install_app(ENV["TEST_APP_PATH"])
    update_app(ENV["APP_PATH"])
    first_time = false
  end
end

After('@upgrade') do |scenario|
  uninstall_apps
end

Before('~@upgrade') do |scenario|
  @scenario_is_outline = (scenario.class == Cucumber::Ast::OutlineTable::ExampleRow)
  if @scenario_is_outline
    scenario = scenario.scenario_outline
  end

  first_time = true

  feature_name = scenario.feature.title
  if FeatureNameMemory.feature_name != feature_name \
      or ENV["RESET_BETWEEN_SCENARIOS"] == "1"
    if ENV["RESET_BETWEEN_SCENARIOS"] == "1"
      log "New scenario - reinstalling apps"
    else
      log "First scenario in feature"
    end

    if first_time
      uninstall_apps
      install_app(ENV["TEST_APP_PATH"])
      install_app(ENV["APP_PATH"])
      first_time = false
    end

    clear_app_data

    FeatureNameMemory.feature_name = feature_name
    FeatureNameMemory.invocation = 1
  else
    FeatureNameMemory.invocation += 1
  end
end

FeatureNameMemory = Class.new
class << FeatureNameMemory
  @feature_name = nil
  attr_accessor :feature_name, :invocation
end