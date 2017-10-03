require 'calabash-android/abase'
require 'modules/assert_module'
require 'modules/element_module'
require 'modules/gestures_module'
require 'modules/input_module'
require 'modules/wait_module'

class MMIAFormPage < Calabash::ABase
  include AssertModule
  include ElementModule
  include GesturesModule
  include InputModule
  include WaitModule

  def trait
    'android.widget.FrameLayout id:"fragment_requisition"'
  end

  def swipe_completely_to_the_right(direction, times)
    swipe_to_selected_direction(direction, times)
  end

  def assert_product_amount_is_equals_to(product, amount)
    product_list_with_amounts = build_product_list_with_amounts
    assert {product_list_with_amounts[product] == amount}
  end

  def add_regimen(regimen_type)
    touch_button_with_id_and_text(@button_add_regimen_id, regimen_type)
  end

  def scroll_to_bottom
    scroll_down_to_component_marked(@button_complete_text, @view_to_scroll_name)
  end

  def set_patient_totals
    bulk_text_for_component_without_text_value(@edit_patient_amount_id, '4')
  end

  def set_regimen_totals
    bulk_text_for_component_without_text_value(@edit_regimen_totals_id, '1')
  end

  def save_mmia_form
    touch_button_with_id_and_text(@button_save_id, @button_save_text)
  end

  def submit_mmia_form
    touch_button_with_id_and_text(@button_complete_id, @button_complete_text)
  end

  def add_amount_to_new_regimens
    bulk_text_for_component_without_text_value(@edit_regimen_totals_id, '1')
  end

  private
  def build_product_list_with_amounts
    return @product_list_with_amounts if @product_list_with_amounts
    product_names = get_elements_by_id(@label_medicine_id).map {|product| product['text']}
    inventory_amounts = get_elements_by_id(@label_inventory_id).map {|amount| amount['text']}
    @product_list_with_amounts = product_names.zip(inventory_amounts).to_h
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @label_medicine_id = 'tv_primary_name'
    @label_inventory_id = 'tv_inventory'
    @button_complete_text = 'Submit for Approval'
    @button_complete_id = 'btn_complete'
    @view_to_scroll_name = 'scrollView'
    @edit_patient_amount_id = 'et_value'
    @edit_regimen_totals_id = 'et_total'
    @button_add_regimen_id = 'tv_name'
    @button_save_id = 'btn_save'
    @button_save_text = 'Save'
  end
end
