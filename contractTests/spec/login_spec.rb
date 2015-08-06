describe "log in to web server" do

  it "should authenticate and return expected json containing login info" do
    response = RestClient.post "http://#{WEB_UAT_URI}/rest-api/login",
      { 'username' => 'superuser', 'password' => 'password1' }.to_json,
      :content_type => :json,
      :accept => :json
    expect(response.code).to eq 200

    body = JSON.parse(response.body)
    expected_body =
      { "userInformation" =>
        { "userName" => "superuser",
          "userFirstName" => "Maafi-de",
          "userLastName" => "Doe",
          "facilityCode" => "F10",
          "facilityName" => "Village Dispensary"
        }
      }

    expect(body).to eq expected_body
  end
end