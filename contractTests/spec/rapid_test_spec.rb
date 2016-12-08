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
        submittedTime: "20161125T120300.000Z",
        programDataFormSignatures: [
        {
            type: "SUBMITTER",
            text: "mystique"
        },
        {
            type: "APPROVER",
            text: "magneto"
        }
        ],
        programDataFormItems: [
        {
            name: "PUB_PHARMACY",
            columnCode: "CONSUME_HIVDETERMINE",
            value: 10
        },
        {
            name: "PUB_PHARMACY",
            columnCode: "POSITIVE_HIVDETERMINE",
            value: 5
        },
        {
            name: "PUB_PHARMACY",
            columnCode: "CONSUME_SYPHILLIS",
            value: 20
        },
        {
            name: "PUB_PHARMACY",
            columnCode: "POSITIVE_SYPHILLIS",
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
    expect(program_form['submittedTime']).to eq 1480046580000
    expect(program_form['programDataFormSignatures'][0]['text']).to eq 'mystique'

    item1 = program_form['programDataFormItems'].detect do |item|
        item['columnCode']=="CONSUME_HIVDETERMINE" && item['name']=="PUB_PHARMACY"
    end
    expect(item1['value']).to eq 10
  end
end
