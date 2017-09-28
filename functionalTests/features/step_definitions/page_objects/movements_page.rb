require 'calabash-android/abase'
require 'modules/input_module'
require 'modules/wait_module'

class MovementsPage < Calabash::ABase
  include InputModule
  include WaitModule

  def trait
    'android.widget.RelativeLayout id:"rl_activity_stock_movements"'
  end

  def open_new_movement_dialog
    wait_for_component_to_appear(@button_new_movement_id)
    touch_button_with_id(@button_new_movement_id)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @button_new_movement_id = 'btn_new_movement'
  end
end


