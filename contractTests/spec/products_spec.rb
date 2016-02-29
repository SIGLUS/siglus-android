describe "Get programs with products information for a facility" do

  it "should return all programs for the facility with products associated with the programs" do
    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/programs-with-products?facilityCode=HF2",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    expect(body['programsWithProducts'].length).to eq 2
    expect(body['programsWithProducts'][0]['programCode']).not_to be_nil
    expect(body['programsWithProducts'][0]['programName']).not_to be_nil

    expect(body['programsWithProducts'][0]['products'][0]['code']).not_to be_nil
    expect(body['programsWithProducts'][0]['products'][0]['primaryName']).not_to be_nil
  end

  it "should return all updated products after modified time" do
    login_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/login",
      { username: 'mystique', password: 'password1' }.to_json,
      :content_type => :json,
      :accept => :json

    facility_id = JSON.parse(login_response.body)['userInformation']['facilityId']

    #if no parameter for afterUpdatedTime, server will return all the products for programs associated with that facility;
    #if afterUpdatedTime is specified, server will only return products updated after "afterUpdatedTime"
    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/latest-programs-with-products?facilityId=#{facility_id}",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    expect(body['programsWithProducts'].length).to eq 2
    expect(body['latestUpdatedTime']).not_to be_nil
    expect(body['programsWithProducts'][0]['programName']).not_to be_nil

    expect(body['programsWithProducts'][0]['products'][0]['code']).not_to be_nil
    expect(body['programsWithProducts'][0]['products'][0]['primaryName']).not_to be_nil


    #given the afterUpdatedTime got from last fetch, no product in response
    afterUpdatedTime = body['latestUpdatedTime']
    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/latest-programs-with-products?facilityId=#{facility_id}&afterUpdatedTime=#{afterUpdatedTime}",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    expect(body['programsWithProducts'].length).to eq 2

    expect(body['programsWithProducts'][0]['products'].length).to eq 0
    expect(body['programsWithProducts'][1]['products'].length).to eq 0

    expect(body['latestUpdatedTime']).not_to be_nil
    expect(body['latestUpdatedTime']).not_to eq afterUpdatedTime
  end

  it "should save kits and get latest products with kits" do

    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/latest-products",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('mystique', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    expect(body['latestUpdatedTime']).not_to be_nil
    latest_products = body['latestProducts']

    expect(latest_products.length).to eq 1266

    kit1 = latest_products.detect { |p| p['product']['code'] == 'SCOD10'}
    expect(kit1['product']['kitProductList'].length).to eq 44
    expect(kit1['product']['kitProductList'][0]['productCode']).to eq '02A03'

    product1 = latest_products.detect { |p| p['product']['code'] == '08S42'}
    expect(product1['product']['kitProductList']).to be_nil
    expect(product1['supportedPrograms'].length).to eq 1
    expect(product1['supportedPrograms'][0]).to eq 'MMIA'
    expect(product1['product']['archived']).to eq false
  end
end
