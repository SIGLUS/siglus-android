describe "submit emergency requisition to web server" do

  it "should sync emergency requisitions to server successfully and return expected response" do

    via_requisition =
    {
      agentCode: "HF2",
      programCode: "VIA",
      emergency: true,
      clientSubmittedTime: "2016-10-27 11:11:20",
      rnrSignatures: [
      {
        type: "SUBMITTER",
        text: "mystique"
      },
      {
        type: "APPROVER",
        text: "magneto"
      }
      ],
      products: [
      {
        productCode: "01A01",
        beginningBalance: 10,
        quantityReceived: 30,
        quantityDispensed: 20,
        stockInHand: 25,
        totalLossesAndAdjustments: 5,
        calculatedOrderQuantity: 15,
        quantityRequested: 20,
        quantityApproved: 15,
        reasonForRequestedQuantity: "reason"
      },
      {
        productCode: "01A02",
        beginningBalance: 100,
        quantityReceived: 300,
        quantityDispensed: 100,
        stockInHand: 300,
        totalLossesAndAdjustments: 0,
        calculatedOrderQuantity: 0,
        quantityRequested: 0,
        quantityApproved: 0,
        reasonForRequestedQuantity: "reason"
      }
      ]
    }

    via_response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/sdp-requisitions",
      via_requisition.to_json, 'Content-Type' => 'application/json',
      'Accept' => 'application/json',
      'Authorization' => http_basic_auth('mystique', 'password1')

    expect(via_response.code).to eq 201

    body = JSON.parse(via_response.body)
    requisition_id = body['requisitionId']

    expect(requisition_id).not_to be_nil

    via_responseDuplicate = RestClient.post "http://#{WEB_DEV_URI}/rest-api/sdp-requisitions",
          via_requisition.to_json, 'Content-Type' => 'application/json',
          'Accept' => 'application/json',
          'Authorization' => http_basic_auth('mystique', 'password1')

    expect(via_responseDuplicate.code).to eq 200
  end
end