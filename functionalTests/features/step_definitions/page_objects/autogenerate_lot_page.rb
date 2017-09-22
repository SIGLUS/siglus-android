require 'calabash-android/abase'
require 'modules/input_module'

class AutogenerateLotPage < Calabash::ABase
  include InputModule

  def trait
    'android.support.v7.widget.AppCompatTextView id:"autogenerate_lot_dialog"'
  end

  def confirm
    touch_button_with_id(@button_confirm_id)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @button_confirm_id = 'btn_confirm_generate'
  end
end
