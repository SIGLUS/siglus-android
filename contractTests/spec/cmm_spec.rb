describe "update CMM" do

  it "should update CMM on the web server" do

    login_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { username: 'mystique', password: 'password1' }.to_json,
      :content_type => :json,
      :accept => :json

    facility_id = JSON.parse(login_response.body)['userInformation']['facilityId']

    cmm_request =

    [
      {
        "productCode":"08S01ZZ",
        "periodBegin":"2016-05-21",
        "periodEnd":"2016-06-20",
        "cmmValue":0.0
      },
      {
        "productCode":"08S01",
        "periodBegin":"2016-05-21",
        "periodEnd":"2016-06-20",
        "cmmValue":0.6666667
      }]

    response = RestClient.put "http://#{WEB_DEV_URI}/rest-api/facilities/#{facility_id}/Cmms",
      cmm_request.to_json, :content_type => :json, :accept => :json,
      'Authorization' => http_basic_auth('mystique', 'password1')

    expect(response.code).to eq 200

    # Put the same json twice, should return same response
    response = RestClient.put "http://#{WEB_DEV_URI}/rest-api/facilities/#{facility_id}/Cmms",
      cmm_request.to_json, :content_type => :json, :accept => :json,
      'Authorization' => http_basic_auth('mystique', 'password1')

    expect(response.code).to eq 200
  end
end