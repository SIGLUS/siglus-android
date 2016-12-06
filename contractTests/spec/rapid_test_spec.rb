describe "Sync stock card data" do

  it "should sync all stock card data to web server" do
    login_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { username: 'mystique', password: 'password1' }.to_json,
      :content_type => :json,
      :accept => :json

    facility_id = JSON.parse(login_response.body)['userInformation']['facilityId']

    rapid_test_data = {
        facilityId: facility_id,
        programCode: "RAPID_TEST",
        periodBegin: "2016-02-21",
        periodEnd: "2016-03-20",
        submittedTime: "2016-11-25 12:30:05",
        programDataFormItems: [
        {
            name: "PUB_PHARMACY",
            columnCode: "HIV-DETERMINE-CONSUME",
            value: 10
        },
        {
            name: "PUB_PHARMACY",
            columnCode: "HIV-DETERMINE-POSITIVE",
            value: 5
        },
        {
            name: "PUB_PHARMACY",
            columnCode: "SYPHILLIS-CONSUME",
            value: 20
        },
        {
            name: "PUB_PHARMACY",
            columnCode: "SYPHILLIS-POSITIVE",
            value: 10
        }
        ]
    }

    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/programData",
      rapid_test_data.to_json, :content_type => :json, :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/programData/facilities/#{facility_id}",
      :content_type => :json, :authorization => http_basic_auth('mystique', 'password1')
    response_body = JSON.parse(response.body)

    expect(response_body['programData'].length).to be 1
    program_form = response_body['programData'][0]
    expect(program_form['programCode']).to eq 'RAPID_TEST'
    expect(program_form['periodBegin']).to eq 1455984000000
    expect(program_form['periodEnd']).to eq 1458403200000
    expect(program_form['submittedTime']).to eq 1480003200000

    item1 = program_form['programDataFormItems'].detect do |item|
        item['columnCode']=="HIV-DETERMINE-CONSUME" && item['name']=="PUB_PHARMACY"
    end
    expect(item1['value']).to eq 10
  end
end
