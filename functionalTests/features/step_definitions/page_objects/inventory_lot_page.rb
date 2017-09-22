require 'calabash-android/abase'
require 'modules/input_module'

class InventoryLotPage < Calabash::ABase
  include InputModule

  def trait
    'android.support.v7.widget.AppCompatTextView id:"dialog_add_new_lot"'
  end

  def submit_lot_with(lot_number, amount, expiration_date)
    set_text_to_input_with_id(lot_number, @edit_lot_number_id)
    set_text_to_input_with_id(amount, @edit_lot_amount_id)
    set_calendar_with_date(expiration_date)
    touch_button_with_id_and_tag(@button_complete_id, @button_complete_tag)
  end

  def submit_lot_without_lot_number(amount, expiration_date)
    submit_lot_with('', amount, expiration_date)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @edit_lot_number_id = 'et_lot_number'
    @edit_lot_amount_id = 'et_soh_amount'
    @button_complete_id = 'btn_complete'
    @button_complete_tag = 'button_complete_add_lot'
  end
end
