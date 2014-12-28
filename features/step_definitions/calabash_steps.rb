require 'calabash-android/calabash_steps'



Given(/^I wait for the Sign in button to appear$/) do
  touch "button text:'Sign in'"
end


Then(/^I wait for the Mendeley oauth login page$/) do
  wait_for( timeout: 30 ) { query( "webview" ) }
  wait_for( timeout: 30 ) { query( "webview css:'*'" ).length != 3 }
end



Then(/^I enter "([^"]*)" into the "([^"]*)" input field$/) do |value, label_name|
  enter_text("webView css:'##{label_name}'",value)
end


Then(/^I touch "([^"]*)"$/) do |arg|
  system "#{default_device.adb_command} shell input keyevent KEYCODE_ENTER"
  sleep(120)
  printf("travis1")
  #touch("webView css:'.controls'")
end


Then(/^I wait for the "([^"]*)" progress dialog to close$/) do |text|

  if text.eql? 'Sync data...'
    # wait for dialog
    unless query("TextView {text CONTAINS '#{text}'}").length == 1

      wait_for(timeout: 1000) { 1 == query("TextView {text CONTAINS '#{text}'}").length }

    printf("travis")
       sleep(120)
       printf("travis1")
    end
  end


  unless query("TextView {text CONTAINS '#{text}'}").length == 0
    # If it does, then wait for it to close...
    wait_for(timeout: 1000) { 0 == query("TextView {text CONTAINS '#{text}'}").length }
  end

end

=begin
Then(/^I can see "(.*?)" on folder list$/) do |arg1|

  preferences = get_preferences("MendeleyPaperReaderPREF")


  printf('tokens'  + preferences.keys.to_s)

  if !preferences.has_key? 'access_token'
    fail(msg="Error. Login fail.")

  end
end
=end


Then(/^Validate if you are in the right activity$/) do


   wait_for(timeout: 60) { 1 == query("TextView text:'My Publications'").length}
end

Then(/^I touch on "([^"]*)" button$/) do |text|

  touch "TextView text:'#{text}'"
end

Given(/^I see logout button$/) do
  system "#{default_device.adb_command} shell input keyevent KEYCODE_MENU"
end

Then(/^I confirm that i want to logout$/) do
  touch "button text:'OK'"
end

