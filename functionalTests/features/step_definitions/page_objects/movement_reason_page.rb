require 'calabash-android/abase'
require 'modules/input_module'

class MovementReasonPage < Calabash::ABase
  include InputModule

  def trait
    'com.android.internal.app.AlertController$RecycleListView id:"select_dialog_listview"'
  end

  def set_movement_reason(reason)
    touch_view_with_text(reason)
  end
end
