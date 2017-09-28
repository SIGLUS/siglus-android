require 'calabash-android/abase'
require 'modules/input_module'

class MovementDetailsPage < Calabash::ABase
  include InputModule

  def trait
    'android.support.v7.widget.AppCompatTextView text:"Movement Details"'
  end

  def submit_movement_type_details_with(reason, amount, signature)
    set_text_to_input_with_id(amount, @edit_amount_id)
    set_text_to_input_with_id(signature, @edit_signature_id)
    touch_view_with_id(@edit_date_id)
    set_calendar_with_date(set_issue_date)
    touch_button_with_id_and_text(@button_done_id, @button_done_text)
    touch_view_with_id(@edit_movement_reason)
    page(MovementReasonPage).await(timeout: 30).set_movement_reason(reason)
    touch_button_with_id(@button_complete_id)
  end

  private
  def set_issue_date
    Time.new(2016, 02, 16)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @edit_amount_id = 'et_lot_amount'
    @edit_signature_id = 'et_movement_signature'
    @edit_movement_reason = 'et_movement_reason'
    @edit_date_id = 'et_movement_date'
    @button_done_id = 'button1'
    @button_done_text = 'Done'
    @button_complete_id = 'btn_complete'
  end
end