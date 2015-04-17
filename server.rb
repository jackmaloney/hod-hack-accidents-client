require 'sinatra'
require 'json'

before '/' do
  content_type :json
end

get '/' do
  data = {
    :status => "OK",
    :data => "Thank you for getting at " + Time.now.to_s
  }
  data.to_json
end

post '/' do
  data = {
    :status => "OK",
    :data => "Thank you for posting at " + Time.now.to_s
  }
  data.to_json
end

