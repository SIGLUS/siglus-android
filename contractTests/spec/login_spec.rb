describe "log in to web server" do

  it "should authenticate and return expected json containing login info" do
    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { 'username' => 'superuser', 'password' => 'password1' }.to_json,
      :content_type => :json,
      :accept => :json
    expect(response.code).to eq 200

    body = JSON.parse(response.body)

    expect(body['userInformation']['userName']).to eq 'superuser'
    expect(body['userInformation']['userFirstName']).to eq 'Super'
    expect(body['userInformation']['userLastName']).to eq 'User'
    expect(body['userInformation']['facilityCode']).to eq 'F10'
    expect(body['userInformation']['facilityId']).not_to be_nil
    expect(body['userInformation']['facilityName']).to eq 'Health Facility 1'
  end
end