require 'calabash-android/abase'
require 'modules/input_module'
require 'modules/wait_module'

class RapidTestReportPage < Calabash::ABase
  include InputModule
  include WaitModule

  def trait
    'android.widget.RelativeLayout id:"fragment_rapid_test_report_form"'
  end

  def set_consume_values
    bulk_text_for_component(@grid_consume_id, '1')
  end

  def set_positive_values
    bulk_text_for_component(@grid_positive_id, '1')
  end

  def submit_rapid_test_report
    wait_for_component_to_appear(@button_submit_for_approval_id)
    touch_button_with_id_and_tag(@button_submit_for_approval_id, @button_submit_for_approval_tag)
  end

  def save_rapid_test_report
    touch_button_with_id_and_text(@button_save_id, @button_save_text)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @grid_consume_id = 'et_consume_rapid_test_report_grid'
    @grid_positive_id = 'et_positive_rapid_test_report_grid'
    @button_submit_for_approval_id = 'btn_complete'
    @button_submit_for_approval_tag = 'btn_action_complete'
    @button_save_id = 'btn_save'
    @button_save_text = 'Save'
  end
end