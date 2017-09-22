module ElementModule
  def get_text_for_element_with_id(element_id)
    query("* id:'#{element_id}'", :getText).first
  end

  def get_elements_by_id(element_id, element_type='*')
    query("#{element_type} id:'#{element_id}'")
  end
end
