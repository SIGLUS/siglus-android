describe "Get programs with products information for a facility" do

  it "should return all programs for the facility with products associated with the programs" do
    response = RestClient.get "http://#{WEB_UAT_URI}/rest-api/programs-with-products?facilityCode=F10",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('superuser', 'password1')
    expect(response.code).to eq 200

    body = JSON.parse(response.body)

    expect(body['programsWithProducts'].length).to eq 4
    expect(body['programsWithProducts'][0]['programCode']).to eq 'TB'
    expect(body['programsWithProducts'][0]['programName']).to eq 'TB'

    expect(body['programsWithProducts'][0]['products'].length).to eq 113
    expect(body['programsWithProducts'][0]['products'][0]['code']).to eq 'P10'
    expect(body['programsWithProducts'][0]['products'][0]['primaryName']).to eq 'Acetylsalicylic Acid, tablet 300mg'
  end
end