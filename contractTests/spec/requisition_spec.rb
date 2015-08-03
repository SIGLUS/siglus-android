require 'rubygems'
require 'rest-client'
require 'base64'
require 'json'

# NOTE: API does not have delete endpoint and therefore can only
# post until reaching the current period. Use UAT env because UAT will
# clean up data regularly
describe "submit requisition to web server" do
  requisition = {
    "programCode" => "MALARIA",
    "agentCode" => "F10",
    "products" => [
      {
        "productCode" => "P151",
        "beginningBalance" => 1000,
        "quantityReceived" => 2000,
        "quantityDispensed" => 2500,
        "totalLossesAndAdjustments" => 0,
        "stockInHand" => 500,
        "newPatientCount" => 500,
        "stockOutDays" => 20,
        "quantityRequested" => 10000,
        "reasonForRequestedQuantity" => "just because"
      }
    ]
  }

  it "should submit successfully and return expected response" do

    response = RestClient.post "http://#{WEB_UAT_URI}/rest-api/requisitions",
      requisition.to_json, 'Content-Type' => 'application/json',
      'Accept' => 'application/json',
      'Authorization' => http_basic_auth('superuser', 'password1')

    expect(response.code).to eq 201

    body = JSON.parse(response.body)

    expect(body["requisitionId"]).not_to be_nil
  end
end

def http_basic_auth(username, password)
  auth = Base64.strict_encode64("#{username}:#{password}")
  return "Basic #{auth}"
end