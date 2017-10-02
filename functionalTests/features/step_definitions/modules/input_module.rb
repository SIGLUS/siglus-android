module InputModule

    def set_text_to_input_with_id(text, input_id)
      enter_text("android.widget.EditText id:'#{input_id}'", text)
    end

    def touch_button_with_id(button_id)
      touch("android.widget.Button id:'#{button_id}'")
    end

    def touch_button_with_id_and_tag(button_id, tag)
      touch("* id:'#{button_id}' tag:'#{tag}'")
    end

    def touch_button_with_id_and_text(button_id, text)
      touch("* id:'#{button_id}' text:'#{text}'")
    end

    def touch_view_with_id(view_id)
      touch("* id:'#{view_id}'")
    end

    def touch_view_with_text(text)
      touch("* text:'#{text}'")
    end

    def touch_menu_button
      touch("* contentDescription:'More options'")
    end

    def search_for(search_text)
      prepare_for_search
      enter_text("android.support.v7.widget.SearchView id:'action_search'", search_text)
    end

    def touch_component_and_enter_text(component, text)
      touch(component)
      clear_text_in(component)
      keyboard_enter_text(text)
    end

    def search_all_components(component, id)
      query("#{component} id:'#{id}'")
    end

    def set_calendar_with_date(date)
      date_format = '%d/%m/%Y'
      formatted_date = date.strftime(date_format)
      set_date("android.widget.DatePicker", formatted_date)
    end

    def check_item_found
      touch("android.widget.CheckBox id:'checkbox' checked:'false'")
    end

    private
    def prepare_for_search
      touch(close_search_button) if element_exists(close_search_button)
      touch(search_button) if element_exists(search_button)
    end

    def close_search_button
      'android.support.v7.internal.widget.TintImageView id:"search_close_btn"'
    end

    def search_button
      'android.support.v7.internal.widget.TintImageView id:"search_button"'
    end
end
