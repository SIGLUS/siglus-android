require 'calabash-android/abase'

class ContinuePage < Calabash::ABase
  include InputModule

  def trait
    'android.support.v7.widget.AppCompatButton id:"button2"'
  end

  def finish_approval
    touch_button_with_id_and_text(@butthon_continue_id, @butthon_continue_text)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @butthon_continue_id = 'button2'
    @butthon_continue_text = 'Continue'
  end
end
