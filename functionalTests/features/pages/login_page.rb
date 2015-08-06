class LoginPage
  def login(username, password)
    touch("view marked:'User Name'")
    wait_for_keyboard
    keyboard_enter_text username

    touch("view marked:'Password'")
    keyboard_enter_text password
    done
  end
end