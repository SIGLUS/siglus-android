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
    requisition_id = body['requisitionId']

    expect(requisition_id).not_to be_nil


    response = RestClient.get "http://#{WEB_UAT_URI}/rest-api/requisitions/#{requisition_id}",
      'Content-Type' => 'application/json',
      'Accept' => 'application/json',
      'Authorization' => http_basic_auth('superuser', 'password1')

    expect(response.code).to eq 200

    body = JSON.parse(response.body)

    expect(body['requisition']['id']).to eq requisition_id
    expect(body['requisition']['programCode']).to eq "MALARIA"
    expect(body['requisition']['agentCode']).to eq "F10"
    expect(body['requisition']['emergency']).to be false
    expect(body['requisition']['products'][0]['productCode']).to eq "P151"
    expect(body['requisition']['requisitionStatus']).to eq "AUTHORIZED"

    #EXAMPLE RESPONSE:

    # {
    #   "requisition":
    #   {
    #     "id":132,
    #     "programCode":"MALARIA",
    #     "agentCode":"F10",
    #     "emergency":false,
    #     "periodStartDate":1396310400000,
    #     "periodEndDate":1398902399000,
    #     "stringPeriodStartDate":"01/04/2014",
    #     "stringPeriodEndDate":"30/04/2014",
    #     "products":[
    #       {
    #         "productCode":"P151",
    #         "beginningBalance":1000,
    #         "quantityReceived":2000,
    #         "quantityDispensed":2500,
    #         "totalLossesAndAdjustments":0,
    #         "stockInHand":500,
    #         "newPatientCount":500,
    #         "stockOutDays":20,
    #         "quantityRequested":10000,
    #         "reasonForRequestedQuantity":"just because",
    #         "calculatedOrderQuantity":26500,
    #         "quantityApproved":10000,
    #         "skipped":false
    #       }
    #     ],
    #     "requisitionStatus":"AUTHORIZED"
    #   }
    # }
  end
end