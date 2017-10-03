module GesturesModule

  def swipe_left_from_component_to(component_to_swipe_from, coordinates_from, coordinates_to)
    pan(component_to_swipe_from, :right, from: coordinates_from, to: coordinates_to)
  end

  def swipe_to_selected_direction(direction, times=1)
    index = 0
    while index < times.to_i
      perform_action('swipe', direction)
      index = index + 1
    end
  end

  def scroll_down_to_component_marked(text, view_name)
    until element_exists("* marked:'#{text}'") do
      scroll(view_name, :down)
    end
  end

  def scroll_up_to_component_marked(text, view_name)
    until element_exists("* marked:'#{text}'") do
      scroll(view_name, :up)
    end
  end
end
