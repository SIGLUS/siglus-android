require 'calabash-android/abase'
require 'modules/input_module'
require 'modules/wait_module'

class RequisitionSelectPeriodPage < Calabash::ABase
  include InputModule
  include WaitModule

  def trait
    'android.widget.LinearLayout id:"ll_select_period"'
  end

  def select_current_period
    touch_view_with_id(@buton_date_container_id)
    end

  def continue_requisition
    wait_for_component_to_appear(@button_next_id)
    touch_button_with_id(@button_next_id)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @buton_date_container_id = 'inventory_date_container'
    @button_next_id = 'btn_select_period_next'
  end

end
