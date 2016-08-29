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
        quantity: 600,
        type: "ADJUSTMENT",
        reasonName: "DISTRICT_DDM",
        occurred: "2015-10-15",
        referenceNumber: "referenceNumber1",
        customProps: {
          signature: "signature",
          SOH: 1000
        },
        lotEventList: [
          {
            lotNumber: "TEST1",
            expirationDate: "2016-10-31",
            quantity: 500,
            customProps: {
              SOH: 500
            },
          },
          {
            lotNumber: "TEST1",
            expirationDate: "2016-11-31",
            quantity: 100,
            customProps: {
              SOH: 600
            }
          }
        ]
      },
      {
        facilityId: facility_id,
        productCode: "08S42",
        quantity: 105,
        type: "ADJUSTMENT",
        reasonName: "PUB_PHARMACY",
        occurred: "2015-10-24",
        referenceNumber: "referenceNumber2",
        requestedQuantity: 600,
        customProps: {
          signature: "signature",
          SOH: 500
        },
        lotEventList: [
          {
            lotNumber: "TEST-A",
            expirationDate: "2016-10-31",
            quantity: 5,
            customProps: {
              SOH: 5
            },
          },
          {
            lotNumber: "TEST-B",
            expirationDate: "2016-11-31",
            quantity: 100,
            customProps: {
              SOH: 100
            }
          }
        ]
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
          signature: "signature",
          SOH: 480
        },
        lotEventList: [
          {
            lotNumber: "TEST3",
            expirationDate: "2016-10-31",
            quantity: 15,
            customProps: {
              SOH: 500
            },
          },
          {
            lotNumber: "TEST4",
            expirationDate: "2016-11-31",
            quantity: 5,
            customProps: {
              SOH: 100
            }
          }
        ]
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
          signature: "signature",
          SOH: 10
        },
        lotEventList: [
          {
            lotNumber: "TEST5",
            expirationDate: "2016-10-31",
            quantity: 5,
            customProps: {
              SOH: 500
            },
          },
          {
            lotNumber: "TEST6",
            expirationDate: "2016-11-31",
            quantity: 5,
            customProps: {
              SOH: 100
            }
          }
        ]
      },
      {
        facilityId: facility_id,
        productCode: "SCOD10",
        quantity: 1,
        type: "ADJUSTMENT",
        reasonName: "UNPACK_KIT",
        occurred: "2015-10-30",
        referenceNumber: "referenceNumber4",
        customProps: {
          signature: "signature",
          SOH: 11
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

     startTime = '2015-10-15'
     endTime = (Date.today + 1).strftime('%Y-%m-%d')
     response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/facilities/#{facility_id}/stockCards?startTime=#{startTime}&endTime=#{endTime}",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    expect(body['stockCards'].length).to be 3
    stock_card1 = body['stockCards'].select{|stock_card| stock_card['product']['code']=="08S42"}
    expect(stock_card1['stockMovementItems'].length).to be 3
    stock_movement1 = stock_card1['stockMovementItems'].select{|stock_movement| stock_movement['documentNumber']=='referenceNumber1'}
    expect(stock_movement1['movementQuantity']).to be 600
    expect(stock_movement1['reason']).to be 'DISTRICT_DDM'
    expect(stock_movement1['extensions']['soh']).to be '1000'
    expect(stock_movement1['occurred']).to be '2015-10-15'
    expect(stock_movement1['lotMovementItems'].length).to be 2
    lot_movement1 = stock_movement1['lotMovementItems'].select{|lot_movement| lot_movement['lotCode']=='TEST1'}
    expect(lot_movement1['quantity']).to be 500
    expect(lot_movement1['extensions']['soh']).to be '500'
  end
end
