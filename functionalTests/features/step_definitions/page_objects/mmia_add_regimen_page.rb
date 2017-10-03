require 'calabash-android/abase'
require 'modules/input_module'

class MMIAAddRegimenPage < Calabash::ABase
  include InputModule

  def trait
    'android.widget.LinearLayout id:"ll_add_regimen"'
  end

  def search_for_regimen(product_name)
    touch_view_with_text(product_name)
    check_item_found
  end

  def submit_regimen
    touch_button_with_id_and_text(@button_next_id, @button_next_text)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @button_next_id = 'btn_next'
    @button_next_text = 'Next'
  end
end