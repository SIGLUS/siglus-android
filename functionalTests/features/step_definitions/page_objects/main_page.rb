require 'calabash-android/abase'
require 'modules/assert_module'

class MainPage < Calabash::ABase
  include AssertModule

  def trait
    'android.widget.ScrollView id:"main_page_view"'
  end

  def isVisible
    assert { current_page? }
  end
end
