describe "update archived product codes to web server" do

  it "should update and sync down archived product codes and return expected http status 200" do
  binding.pry
  login_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
        { username: 'mystique', password: 'password1' }.to_json,
        :content_type => :json,
        :accept => :json

      facility_id = JSON.parse(login_response.body)['userInformation']['facilityId']

    syncUpResponse = RestClient.post 'http://#{WEB_DEV_URI}/rest-api/facilities/"#{facility_id}"/archivedProducts',
      {["08S01ZZ"]}.to_json,
      :content_type => :json,
      :accept => :json,
      'Authorization' => http_basic_auth('mystique', 'password1')

    expect(syncUpResponse.code).to eq 200

    syncDownResponse = RestClient.get 'http://#{WEB_DEV_URI}/rest-api/facilities/"#{facility_id}"/archivedProducts',
      :content_type => :json,
      :accept => :json,
      'Authorization' => http_basic_auth('mystique', 'password1')

    body = JSON.parse(syncDownResponse.body)
    expect(body['archivedProductCodes'].length).to eq 1
    expect(body['archivedProductCodes'][0]).to eq "08S01ZZ"

    expect(syncDownResponse.code).to eq 200
  end
end