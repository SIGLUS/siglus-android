require 'calabash-android/abase'
require 'modules/wait_module'

class RequisitionInitialPage < Calabash::ABase
  include WaitModule

  def trait
    'android.widget.ScrollView id:"sc_requisition"'
  end

  def wait_for_the_screen_to_be_initialized()
    wait_for_component_to_appear_identified_by_text(@initial_text)
  end



  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @initial_text = 'No Requisition Balancete has been created.'
  end
end