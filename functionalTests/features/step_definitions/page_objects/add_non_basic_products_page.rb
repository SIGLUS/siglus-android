require 'calabash-android/abase'
require 'modules/input_module'
require 'modules/wait_module'

class AddNonBasicProductsPage < Calabash::ABase
  include InputModule
  include WaitModule

  def trait
    'android.widget.RelativeLayout id:"rl_activity_add_products"'
  end

  def search_product_with_name(product_name)
    wait_for_element_to_disappear(@loading_text)
    search_for(product_name)
  end

  def check_product_with_name
    touch_view_with_id(@checkbox_check_products_id)
  end

  def add_selected_products
    touch_view_with_id(@button_add_selected_products_id)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @checkbox_check_products_id = 'checkbox'
    @button_add_selected_products_id = 'btn_add_products'
    @loading_text = 'Downloading Non Basic Products'
  end
end