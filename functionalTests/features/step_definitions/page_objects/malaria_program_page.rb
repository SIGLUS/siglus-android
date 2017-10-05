require 'calabash-android/abase'
require 'modules/assert_module'
require 'modules/element_module'
require 'modules/input_module'
require 'modules/wait_module'

class MalariaProgramPage < Calabash::ABase
  include AssertModule
  include ElementModule
  include InputModule
  include WaitModule

  def trait
    'android.widget.RelativeLayout id:"fragment_patient_data_report_form"'
  end

  def set_values
    bulk_text_for_component_with_tag(@value_treatment_6x1, @edit_tag, '1')
    bulk_text_for_component_with_tag(@value_treatment_6x2, @edit_tag, '2')
    bulk_text_for_component_with_tag(@value_treatment_6x3, @edit_tag, '3')
    bulk_text_for_component_with_tag(@value_treatment_6x4, @edit_tag, '4')
    bulk_text_for_component_with_tag(@value_existing_stock_6x1, @edit_tag, '1')
    bulk_text_for_component_with_tag(@value_existing_stock_6x2, @edit_tag, '2')
    bulk_text_for_component_with_tag(@value_existing_stock_6x3, @edit_tag, '3')
    bulk_text_for_component_with_tag(@value_existing_stock_6x4, @edit_tag, '4')
  end

  def save_patient_data_report
    touch_button_with_id_and_text(@button_save_id, @button_save_text)
  end

  def initialize(world, transition_duration = 0.5)
    super(world, transition_duration)
    @value_treatment_6x1 = 'et_current_treatment_6x1'
    @value_treatment_6x2 = 'et_current_treatment_6x2'
    @value_treatment_6x3 = 'et_current_treatment_6x3'
    @value_treatment_6x4 = 'et_current_treatment_6x4'
    @edit_existing_stock_6x1_id = 'et_existing_stock_6x1'
    @value_existing_stock_6x2 = 'et_existing_stock_6x2'
    @value_existing_stock_6x3 = 'et_existing_stock_6x3'
    @value_existing_stock_6x4 = 'et_existing_stock_6x4'
    @edit_tag = 'enabled'
    @button_save_text = 'Save'
    @button_save_id = 'btn_save'
  end
end
