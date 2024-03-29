require 'calabash-android/abase'
require 'modules/input_module'

class StockOverviewPage < Calabash::ABase
  include InputModule

  def trait
    'android.widget.FrameLayout id:"stock_card_container"'
  end

  def search_for_product(product_name)
    search_for(product_name)
  end

  def open_new_movement_for_product_found
    touch_view_with_id(@label_product_name_id)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @label_product_name_id = 'tv_product_name'
  end
end
