require 'calabash-android/abase'
require 'modules/input_module'
require 'modules/element_module'

class LoginPage < Calabash::ABase
  include InputModule
  include ElementModule

  def trait
    'android.widget.Button text:"LOG IN"'
  end

  def login(username, password)
    set_text_to_input_with_id(username, @edit_username_id) unless current_user
    set_text_to_input_with_id(password, @edit_password_id)
    touch_button_with_id(@button_log_in_id)
  end

  def current_user
    username = get_text_for_element_with_id(@edit_username_id)
    return username.empty? ? nil : username
  end

  def initialize(world, transition_duration=0.5)
    super(world, transition_duration)
    @edit_username_id = 'tx_username'
    @edit_password_id = 'tx_password'
    @button_log_in_id = 'btn_login'
  end
end
