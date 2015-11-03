# NOTE: API does not have delete endpoint and therefore can only
# post until reaching the current period. Use UAT env because UAT will
# clean up data regularly
describe "submit requisition to web server" do

  it "should sync VIA requisition to server successfully and return expected response" do

    requisition =
    {
      agentCode: "F10",
      programCode: "ESS_MEDS",
      clientSubmittedTime: "2015-10-27 11:11:20",
      products: [
      {
        productCode: "P1",
        beginningBalance: 10,
        quantityReceived: 0,
        quantityDispensed: 0,
        stockInHand: 20,
        totalLossesAndAdjustments: 10,
        calculatedOrderQuantity: 0,
        quantityRequested: 0,
        reasonForRequestedQuantity: "reason"
      },
      {
        productCode: "P2",
        beginningBalance: 100,
        quantityReceived: 300,
        quantityDispensed: 100,
        stockInHand: 300,
        totalLossesAndAdjustments: 0,
        calculatedOrderQuantity: 0,
        quantityRequested: 0,
        reasonForRequestedQuantity: "reason"
      }
      ] ,
      patientQuantifications: [
      {
        category: "consultation",
        total: 100
      }
      ]
    }

    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/requisitions",
      requisition.to_json, 'Content-Type' => 'application/json',
      'Accept' => 'application/json',
      'Authorization' => http_basic_auth('superuser', 'password1')

    expect(response.code).to eq 201

    body = JSON.parse(response.body)
    requisition_id = body['requisitionId']

    expect(requisition_id).not_to be_nil

    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/requisitions/#{requisition_id}",
      'Content-Type' => 'application/json',
      'Accept' => 'application/json',
      'Authorization' => http_basic_auth('superuser', 'password1')

    expect(response.code).to eq 200

    body = JSON.parse(response.body)

    expect(body['requisition']['id']).to eq requisition_id
    expect(body['requisition']['programCode']).to eq "ESS_MEDS"
    expect(body['requisition']['agentCode']).to eq "F10"
    expect(body['requisition']['emergency']).to be false
    expect(body['requisition']['requisitionStatus']).to eq "AUTHORIZED"

  end

  it "should sync MMIA requisition to server successfully and return expected response" do
    requisition =
    {
      agentCode: "F10",
      programCode: "MMIA",
      clientSubmittedNotes: "I don't know",
      clientSubmittedTime: "2015-10-27 11:11:20",
      products: [
      {
        productCode: "08S23",
        beginningBalance: 30,
        quantityReceived: 10,
        quantityDispensed: 20,
        totalLossesAndAdjustments: 0,
        stockInHand: 20,
        quantityRequested: 0,
        reasonForRequestedQuantity: "reason",
        expirationDate: "10/10/2016"
      },
      {
        productCode: "08S17",
        beginningBalance: 10,
        quantityReceived: 10,
        quantityDispensed: 10,
        totalLossesAndAdjustments: 0,
        stockInHand: 10,
        quantityRequested: 0,
        reasonForRequestedQuantity: "reason",
        expirationDate: "10/10/2016"
      }
      ],
      regimens: [
      {
        code: "001",
        name: "AZT+3TC+NVP",
        patientsOnTreatment: 1
      },
      {
        code: "002",
        name: "TDF+3TC+EFV",
        patientsOnTreatment: 2
      },
      {
        code: "003",
        name: "AZT+3TC+EFV",
        patientsOnTreatment: 3
      },
      {
        code: "004",
        name: "d4T 30+3TC+NVP",
        patientsOnTreatment: 1
      },
      {
        code: "005",
        name: "d4T 30+3TC+EFV",
        patientsOnTreatment: 1
      },
      {
        code: "006",
        name: "AZT+3TC+LPV/r",
        patientsOnTreatment: 1
      },
      {
        code: "007",
        name: "TDF+3TC+LPV/r",
        patientsOnTreatment: 1
      },
      {
        code: "008",
        name: "ABC+3TC+LPV/r",
        patientsOnTreatment: 1
      },
      {
        code: "009",
        name: "d4T+3TC+NVP(3DFC Baby)",
        patientsOnTreatment: 1
      },
      {
        code: "010",
        name: "d4T+3TC+LPV/r(2DFC Baby + LPV/r)",
        patientsOnTreatment: 1
      },
      {
        code: "011",
        name: "d4T+3TC+ABC(2DFC Baby + ABC)",
        patientsOnTreatment: 1
      },
      {
        code: "012",
        name: "d4T+3TC+EFV(2DFC Baby + EFV)",
        patientsOnTreatment: 1
      },
      {
        code: "013",
        name: "AZT60+3TC+NVP(3DFC)",
        patientsOnTreatment: 1
      },
      {
        code: "014",
        name: "AZT60+3TC+EFV(2DFC + EFV)",
        patientsOnTreatment: 1
      },
      {
        code: "015",
        name: "AZT60+3TC+ABC(2DFC + ABC)",
        patientsOnTreatment: 1
      },
      {
        code: "016",
        name: "AZT60+3TC+LPV/r(2DFC + LPV/r)",
        patientsOnTreatment: 1
      },
      {
        code: "017",
        name: "ABC+3TC+LPV/r",
        patientsOnTreatment: 1
      },
      {
        code: "018",
        name: "ABC+3TC+EFZ",
        patientsOnTreatment: 1
      }
      ],
      patientQuantifications: [
      {
        category: "New Patients",
        total: 1
      },
      {
        category: "Sustaining",
        total: 1
      },
      {
        category: "Alteration",
        total: 1
      },
      {
        category: "PTV",
        total: 1
      },
      {
        category: "PPE",
        total: 26
      },
      {
        category: "Total Month Dispense",
        total: 1
      },
      {
        category: "Total Patients",
        total: 30
      }
      ]
    }


    response = RestClient.post "http://#{WEB_DEV_URI}/rest-api/requisitions",
      requisition.to_json, 'Content-Type' => 'application/json',
      'Accept' => 'application/json',
      'Authorization' => http_basic_auth('superuser', 'password1')

    expect(response.code).to eq 201

    body = JSON.parse(response.body)
    requisition_id = body['requisitionId']

    expect(requisition_id).not_to be_nil

    response = RestClient.get "http://#{WEB_DEV_URI}/rest-api/requisitions?facilityCode=F10",
      'Content-Type' => 'application/json',
      'Accept' => 'application/json',
      'Authorization' => http_basic_auth('superuser', 'password1')

    expect(response.code).to eq 200

    body = JSON.parse(response.body)

    expect(body['requisitions']['periodStartDate']).not_to be_nil
    expect(body['requisition']['programCode']).to eq "MMIA"
    expect(body['requisition']['agentCode']).to eq "F10"
    expect(body['requisition']['emergency']).to be false
    expect(body['requisition']['requisitionStatus']).to eq "AUTHORIZED"
  end
end