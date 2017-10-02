require 'calabash-android/abase'
require 'modules/input_module'

class RequisitionAddProductsPage < Calabash::ABase
  include InputModule

  def trait
    'android.widget.LinearLayout id:"ll_add_drugs_to_via"'
  end

  def search_for_product(product_name)
    search_for(product_name)
    check_item_found
  end

  def set_amount(amount)
    set_text_to_input_with_id(amount, @edit_amount_id)
  end

  def submit_new_product_to_requisition
    touch_button_with_id(@button_complete_id)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @button_complete_id = 'btn_complete'
    @edit_amount_id = 'tx_quantity'
  end

end