describe "Sync stock card data" do

  it "should sync all stock card data to web server" do
    login_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { username: 'mystique', password: 'password1' }.to_json,
      :content_type => :json,
      :accept => :json

    facility_id = JSON.parse(login_response.body)['userInformation']['facilityId']

    stock_card_data =
      [
      {
        facilityId: facility_id,
        productCode: "08S42",
        quantity: 1000,
        type: "ADJUSTMENT",
        reasonName: "DISTRICT_DDM",
        occurred: "2015-10-15",
        referenceNumber: "referenceNumber1",
        customProps: {
          expirationDates: "10/10/2017, 12/12/2017",
          signature: "signature",
          SOH: 1000
        }
      },
      {
        facilityId: facility_id,
        productCode: "08S42",
        quantity: 500,
        type: "ADJUSTMENT",
        reasonName: "PUB_PHARMACY",
        occurred: "2015-10-24",
        referenceNumber: "referenceNumber2",
        customProps: {
          expirationDates: "10/10/2017, 12/12/2017",
          signature: "signature",
          SOH: 500
        }
      },
      {
        facilityId: facility_id,
        productCode: "08S42",
        quantity: 20,
        type: "ADJUSTMENT",
        reasonName: "LOANS_DEPOSIT",
        occurred: "2015-10-30",
        referenceNumber: "referenceNumber3",
        customProps: {
          expirationDates: "10/10/2017, 12/12/2017",
          signature: "signature",
          SOH: 480
        }
      },
      {
        facilityId: facility_id,
        productCode: "08S34B",
        quantity: 10,
        type: "ADJUSTMENT",
        reasonName: "LOANS_RECEIVED",
        occurred: "2015-10-30",
        referenceNumber: "referenceNumber4",
        customProps: {
          expirationDates: "10/10/2019",
          signature: "signature",
          SOH: 10
        }
      }
      ]

    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/facilities/#{facility_id}/stockCards",
      stock_card_data.to_json, :content_type => :json, :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200
  end

  it "should fetch stock card from web server " do
    login_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { username: 'mystique', password: 'password1' }.to_json,
      :content_type => :json,
      :accept => :json

     facility_id = JSON.parse(login_response.body)['userInformation']['facilityId']

     startTime = Date.today.strftime('%Y-%m-%d')
     endTime = (Date.today + 1).strftime('%Y-%m-%d')
     response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/facilities/#{facility_id}/stockCards?startTime=#{startTime}&endTime=#{endTime}",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200
    expect(body['stockCards'][0]['product']['code']).to eq '08S42'
    expect(body['stockCards'].length).to be >= 2
  end
end