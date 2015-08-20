require 'rubygems'
require 'rest-client'
require 'json'
require 'base64'

WEB_UAT_URI = "localhost:9091"

def http_basic_auth(username, password)
  auth = Base64.strict_encode64("#{username}:#{password}")
  return "Basic #{auth}"
end