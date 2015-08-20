describe "Get programs with products information for a facility" do

  it "should return all programs for the facility with products associated with the programs" do
    response = RestClient.get "http://#{WEB_UAT_URI}/rest-api/programs-with-products?facilityCode=F10",
      :content_type => :json,
      :accept => :json,
      :authorization => http_basic_auth('superuser', 'password1')

    body = JSON.parse(response.body)
    expect(response.code).to eq 200

    expect(body['programsWithProducts'].length).to eq 2
    expect(body['programsWithProducts'][0]['programCode']).not_to be_nil
    expect(body['programsWithProducts'][0]['programName']).not_to be_nil

    expect(body['programsWithProducts'][0]['products'][0]['code']).not_to be_nil
    expect(body['programsWithProducts'][0]['products'][0]['primaryName']).not_to be_nil
  end
end