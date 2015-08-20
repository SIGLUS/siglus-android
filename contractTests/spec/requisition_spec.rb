# NOTE: API does not have delete endpoint and therefore can only
# post until reaching the current period. Use UAT env because UAT will
# clean up data regularly
describe "submit requisition to web server" do

  it "should sync MMIA requisition to server successfully and return expected response" do

    requisition = {
      programCode: "MMIA",
      agentCode: "F10",
      products: [
      {
        productCode: "P6",
        beginningBalance: 1000,
        quantityReceived: 2000,
        quantityDispensed: 2500,
        totalLossesAndAdjustments: 0,
        quantityRequested: 1000,
        expirationDate: "12/31/2015",
        reasonForRequestedQuantity: "justbecause"
      }
      ],
      patientQuantification: [
      {
        category: "adult",
        value: 100
      },
      {
        category: "child",
        value: 300
      }
      ],
      regimens: [
      {
        code: "001",
        patientsOnTreatment: 200
      },
      {
        code: "002",
        patientsOnTreatment: 200
      },
      {
        code: "003",
        patientsOnTreatment: 200
      }
      ]
    }


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
    expect(body['requisition']['programCode']).to eq "MMIA"
    expect(body['requisition']['agentCode']).to eq "F10"
    expect(body['requisition']['emergency']).to be false
    expect(body['requisition']['requisitionStatus']).to eq "AUTHORIZED"
  end
end