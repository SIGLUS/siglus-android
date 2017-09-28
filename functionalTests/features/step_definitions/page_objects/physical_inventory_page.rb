require 'calabash-android/abase'
require 'modules/assert_module'
require 'modules/input_module'

class PhysicalInventoryPage < Calabash::ABase
  include AssertModule
  include InputModule

  def trait
    'android.widget.LinearLayout id:"ll_physical_inventory"'
  end

  def search_for_product(product_name)
    search_for(product_name)
  end

  def open_new_movement_for_product_found
    touch_view_with_id(@label_product_name_id)
  end

  def set_amount(amount)
    set_text_to_input_with_id(amount,@edit_amount_id)
  end

  def isVisible
    assert { current_page? }
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @label_product_name_id = 'tv_product_name'
    @edit_amount_id = 'et_lot_amount'
  end

end
