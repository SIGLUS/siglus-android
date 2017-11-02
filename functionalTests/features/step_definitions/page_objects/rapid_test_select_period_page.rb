require 'calabash-android/abase'
require 'modules/input_module'

class RapidTestSelectPeriodPage < Calabash::ABase
  include InputModule

  def trait
    'android.widget.ScrollView id:"sv_rapid_test"'
  end

  def continue_working_on_report
    touch_view_with_id(@btn_create_patient_data_report)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @btn_create_patient_data_report = 'btn_create_patient_data_report'
  end
end