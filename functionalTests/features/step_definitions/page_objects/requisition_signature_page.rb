require 'calabash-android/abase'

class RequisitionSignaturePage < Calabash::ABase
  include InputModule

  def trait
    'android.widget.LinearLayout id:"ll_inventory_signature"'
  end

  def sign_and_approve_requisition(signature)
    set_text_to_input_with_id(signature, @edit_signature_id)
    touch_button_with_id_and_text(@button_approve_id, @button_approve_text)
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @edit_signature_id = 'et_signature'
    @button_approve_id = 'btn_done'
    @button_approve_text = 'Approve'
  end


end
