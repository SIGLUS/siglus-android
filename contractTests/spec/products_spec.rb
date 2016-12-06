describe "Get programs with products information for a facility" do

  it "should save kits and get latest products with kits" do

    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/latest-products",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('stock_card', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    expect(body['latestUpdatedTime']).not_to be_nil
    latest_products = body['latestProducts']

    expect(latest_products.length).to eq 1268

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
