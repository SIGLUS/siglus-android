describe "update version to web server" do

  it "should update version and return expected http status 200" do
    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/update-app-info",
      { 'facilityCode' => 'HF2', 'userName' => 'superuser', 'version' => '2.0' }.to_json,
      :content_type => :json,
      :accept => :json,
      'Authorization' => http_basic_auth('mystique', 'password1')

    expect(response.code).to eq 200
  end
end