describe "log in to web server" do

  it "should authenticate and return expected json containing login info" do
    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { 'username' => 'mystique', 'password' => 'password1' }.to_json,
      :content_type => :json,
      :accept => :json
    expect(response.code).to eq 200

    body = JSON.parse(response.body)

    expect(body['userInformation']['userName']).to eq 'mystique'
    expect(body['userInformation']['userFirstName']).to eq 'Raven'
    expect(body['userInformation']['userLastName']).to eq 'Darkholme'
    expect(body['userInformation']['facilityCode']).to eq 'HF2'
    expect(body['userInformation']['facilityId']).not_to be_nil
  end
end