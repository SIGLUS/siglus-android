require 'calabash-android/abase'
require 'modules/input_module'

class MovementsTypePage < Calabash::ABase
  include InputModule

  def trait
    'com.android.internal.app.AlertController$RecycleListView id:"select_dialog_listview"'
  end

  def add_new_issue
    touch_button_with_id_and_text(@label_option_id, @label_issues_text)
  end

  def add_new_entry
    touch_button_with_id_and_text(@label_option_id, @label_entries_text)
  end

  def add_new_positive_adjustment
    touch_button_with_id_and_text(@label_option_id, @label_positive_adjustment)
  end

  def add_new_negative_adjustment
    touch_button_with_id_and_text(@label_option_id, @label_negative_adjustment)
  end

  def initialize(world, transition_duration=1000)
    super(world, transition_duration)
    @label_option_id = 'tv_option'
    @label_issues_text = 'Issues'
    @label_entries_text = 'Entries'
    @label_positive_adjustment = 'Positive Adjustments'
    @label_negative_adjustment = 'Negative Adjustments'
  end
end
