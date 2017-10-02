require 'calabash-android/abase'
require 'modules/assert_module'
require 'modules/element_module'
require 'modules/gestures_module'
require 'modules/input_module'

class RequisitionConsultationReportPage < Calabash::ABase
  include AssertModule
  include ElementModule
  include GesturesModule
  include InputModule

  def trait
    'android.widget.LinearLayout id:"ll_requisition_report_consultation"'
  end

  def set_consultations_performed(consultations_performed)
    set_text_to_input_with_id(consultations_performed, @edit_consultations_performed_id)
  end

  def swipe_to_theoretical_stock
    query_component_to_swipe_from = '* id:"et_request_amount"'
    coordinates_from = {x: 0, y: 100}
    coordinates_to = {x: 700, y:100}
    swipe_left_from_component_to(query_component_to_swipe_from, coordinates_from, coordinates_to)
  end

  def assert_product_amount_is_equals_to(product, amount)
    product_list_with_amounts = build_product_list_with_amounts
    assert{product_list_with_amounts[product] == amount}
  end

  def add_product_to_requisition
    touch_menu_button
    touch_button_with_id_and_text(@button_add_products_id, @button_add_products_text)
  end

  def swipe_completely_to_the_right(direction, times)
    swipe_to_selected_direction(direction,times)
  end

  def set_quantity_requested_per_product(quantity)
    components_found = search_all_components(@edit_quantity_requested_class, @edit_quantity_requested_id)
    components_found = components_found.first components_found.size-1
    for component in components_found
      touch_component_and_enter_text(component, quantity)
    end
  end

  def save_requisition
    touch_button_with_id_and_text(@button_save_id, @button_save_text)
  end

  def submit_requisition_for_approval
    touch_button_with_id_and_tag(@button_submit_for_approval_id, @button_submit_for_approval_tag)
  end

  private
  def build_product_list_with_amounts
    return @product_list_with_amounts if @product_list_with_amounts
    product_names = get_elements_by_id(@label_product_name_id).map {|product| product['text']}
    theoretical_amounts = get_elements_by_id(@label_theoretical_amount_id).map {|amount| amount['text']}
    @product_list_with_amounts = product_names.zip(theoretical_amounts).to_h
  end


  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @edit_consultations_performed_id = 'et_external_consultations_performed'
    @label_product_name_id = 'tx_product_name'
    @label_theoretical_amount_id = 'tx_theoretical'
    @button_add_products_id = 'title'
    @button_add_products_text = 'Add Products'
    @edit_quantity_requested_id = 'et_request_amount'
    @edit_quantity_requested_class = 'android.widget.EditText'
    @button_save_id = 'btn_save'
    @button_save_text = 'Save'
    @button_submit_for_approval_id = 'btn_complete'
    @button_submit_for_approval_tag = 'btn_action_complete'
  end

end