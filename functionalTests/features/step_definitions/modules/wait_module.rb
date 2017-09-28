require 'calabash-android/calabash_steps'

module WaitModule
  def wait_for_view_to_disappears(view_name, timeout=10, post_timeout=0.2)
    view_id = "view_#{view_name}".downcase!.gsub(/ /, '_')
    view_query = "* id:'#{view_id}'"
    wait_for_element_does_not_exist(view_query, {timeout: timeout, post_timeout: post_timeout})
  end

  def wait_for_view_to_appears(view_name, timeout=10, post_timeout=0.2)
    view_id = "view_#{view_name}".downcase!.gsub(/ /, '_')
    view_query = "* id:'#{view_id}'"
    wait_for_element_exists(view_query, {timeout: timeout, post_timeout: post_timeout})
  end

  def wait_for_component_to_appear(id, timeout=10, post_timeout=1)
    query_string = "* id:'#{id}'"
    wait_for_element_exists(query_string, { timeout: timeout , post_timeout: post_timeout})
  end
end
