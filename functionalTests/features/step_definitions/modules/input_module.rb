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

    def touch_view_with_id(view_id)
      touch("* id:'#{view_id}'")
    end

    def touch_view_with_text(text)
      touch("* text:'#{text}'")
    end

    def search_for(search_text)
      prepare_for_search
      enter_text("android.support.v7.widget.SearchView id:'action_search'", search_text)
    end

    def set_calendar_with_date(expiration_date)
      date_format = '%d/%m/%Y'
      formatted_date = expiration_date.strftime(date_format)
      set_date("android.widget.DatePicker", formatted_date)
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
