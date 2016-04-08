describe "log in to web server" do

  it "should authenticate and return expected json containing login info" do
    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { 'username' => 'stock_card', 'password' => 'password1' }.to_json,
      :content_type => :json,
      :accept => :json
    expect(response.code).to eq 200

    body = JSON.parse(response.body)

    expect(body['userInformation']['userName']).to eq 'stock_card'
    expect(body['userInformation']['facilityCode']).to eq 'F_STOCKCARD'
    expect(body['userInformation']['facilityId']).not_to be_nil

    expect(body['facilitySupportedPrograms'].length).to eq 5
    tb_program = body['facilitySupportedPrograms'].detect {|p| p['programCode'] == 'TB'}
    expect(tb_program['parentCode']).to eq 'VIA'
    expect(tb_program['programName']).to eq 'TB'
    expect(tb_program['isSupportEmergency']).not_to be_nil
  end
end