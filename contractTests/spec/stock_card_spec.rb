describe "Sync stock card data" do

  it "should sync all stock card data to web server" do
    login_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { username: 'superuser', password: 'password1' }.to_json,
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
        customProps: {
          expirationDates: "10/10/2016, 12/12/2016"
        }
      },
      {
        facilityId: facility_id,
        productCode: "08S42",
        quantity: 500,
        type: "ADJUSTMENT",
        reasonName: "PUB_PHARMACY",
        occurred: "2015-10-24",
        customProps: {
          expirationDates: "10/10/2017, 12/12/2017"
        }
      },
      {
        facilityId: facility_id,
        productCode: "08S42",
        quantity: 20,
        type: "ADJUSTMENT",
        reasonName: "LOANS_DEPOSIT",
        occurred: "2015-10-30",
        customProps: {
          expirationDates: "10/10/2018, 12/12/2018"
        }
      },
      {
        facilityId: facility_id,
        productCode: "08S34B",
        quantity: 10,
        type: "ADJUSTMENT",
        reasonName: "LOANS_DEPOSIT",
        occurred: "2015-10-30",
        customProps: {
          expirationDates: "10/10/2019"
        }
      }
      ]

    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/facilities/#{facility_id}/stockCards",
      stock_card_data.to_json, :content_type => :json, :accept => :json,
      :authorization => http_basic_auth('superuser', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200
  end
end