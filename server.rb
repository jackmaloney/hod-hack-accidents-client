require 'sinatra'

before '/' do
  content_type :json
end

get '/' do
  '{"status":"OK","data":"Thankyou for getting"}'
end

post '/' do
  '{"status":"OK","data":"Thankyou for posting"}'
end

