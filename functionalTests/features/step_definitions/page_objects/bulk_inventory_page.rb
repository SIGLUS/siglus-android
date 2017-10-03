require 'calabash-android/abase'
require 'modules/assert_module'
require 'modules/element_module'
require 'modules/input_module'
require 'modules/wait_module'

class BulkInventoryPage < Calabash::ABase
  include AssertModule
  include ElementModule
  include InputModule
  include WaitModule

  def trait
    'android.widget.TextView text:"Initial Inventory"'
  end

  def search_product_with_name(product_name)
    search_for(product_name)
  end

  def open_add_lot_for_product_found
    touch_view_with_id(@button_add_lot_id)
  end

  def set_no_stock_for_product_found
    touch_view_with_id(@button_no_stock_id)
  end

  def delete_added_lot_in_position(index)
    touch_button_with_id_and_tag(@button_delete_lot_id, index)
  end

  def assert_stock_on_hand_total_for_product_found_is(amount)
    stock_on_hand = get_text_for_element_with_id(@label_stock_on_hand_id)
    assert {stock_on_hand == amount}
  end

  def assert_product_is_not_listed(product_name)
    products = get_elements_by_id(@label_product_name_id)
    product_names = products.map { |product| product['text'] }
    assert { not product_names.any? { |name| name =~ /#{Regex.quote(product_name)}/ }}
  end

  def submit
    wait_for_component_to_appear(@button_submit_id)
    touch_button_with_id_and_tag(@button_submit_id, @button_submit_tag)
  end

  def save
    touch_button_with_id(@button_save_id)
  end

  def isVisible
    assert { current_page? }
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @button_add_lot_id = 'btn_add_new_lot'
    @label_stock_on_hand_id = 'tv_sho_amount'
    @button_no_stock_id = 'btn_no_stock'
    @button_delete_lot_id = 'btn_delete_lot'
    @button_submit_id = 'btn_complete'
    @button_submit_tag = 'btn_action_complete'
    @button_save_id = 'btn_save'
    @label_product_name_id = 'tv_product_name'
  end
end
