require 'calabash-android/calabash_steps'



Given(/^I wait for the Sign in button to appear$/) do
  touch "button text:'Sign in'"
  #puts('Step 1')
end


Then(/^I wait for the Mendeley oauth login page$/) do
  wait_for( timeout: 30 ) { query( "webview" ) }
  wait_for( timeout: 30 ) { query( "webview css:'*'" ).length != 3 }
  
  #puts('Step 2')
end



Then(/^I enter "([^"]*)" into the "([^"]*)" input field$/) do |value, label_name|
  enter_text("webView css:'##{label_name}'",value)
  #puts('Step 3')
end


Then(/^I touch "([^"]*)"$/) do |arg|
  system "#{default_device.adb_command} shell input keyevent KEYCODE_ENTER"
#puts('Step 4')
end


Then(/^I wait for the "([^"]*)" progress dialog to close$/) do |text|

  if text.eql? 'Sync data...'
    # wait for dialog
    unless query("TextView {text CONTAINS '#{text}'}").length == 1

      wait_for(timeout: 10000) { 1 == query("TextView {text CONTAINS '#{text}'}").length }

    end
  end


  #q = query("TextView {text CONTAINS '#{text}'}")
  #until q.empty?
  #  puts('Sync data...');
  #  q = query("TextView {text CONTAINS '#{text}'}")
  #end


  unless query("TextView {text CONTAINS '#{text}'}").length == 0
     #If it does, then wait for it to close...
    wait_for(timeout: 10000) { 0 == query("TextView {text CONTAINS '#{text}'}").length }
  end
  
  #puts('Step 5')

end


Then(/^I validate if it's the right activity - MainMenuActivity$/) do


   wait_for(timeout: 90) { 1 == query("TextView text:'My Publications'").length}
   #puts('Step 6')
end

Then(/^I touch on "([^"]*)" button$/) do |text|

  touch "TextView text:'#{text}'"
  #puts('Step 7')
end

Given(/^I see logout button$/) do
  system "#{default_device.adb_command} shell input keyevent KEYCODE_MENU"
  #puts('Step 8')
end

Then(/^I confirm that i want to logout$/) do
  touch "button text:'OK'"
  #puts('Step 9')
end

Given(/^I see Settings button$/) do
  system "#{default_device.adb_command} shell input keyevent KEYCODE_MENU"
end

Then(/^I validate if it's the right activity - Settings$/) do
  wait_for(timeout: 60) { 1 == query("TextView text:'Synchronization options'").length}
end


Then(/^I check the checkBox "([^"]*)"$/) do |arg|
  touch "CheckBox id:'checkBoxSyncOnLoad'"
end


Then(/^I confirm that the checkBox "([^"]*)" are checked$/) do |arg|
  'true' == query("CheckBox id:'checkBoxSyncOnLoad'",:isChecked)

end

Then(/^I press refresh button$/) do
  touch "ActionMenuItemView id:'menu_refresh'"
end

Then(/^I check if the file has been downloaded$/) do

  1 == query("ImageView tag:'open'").length
end

Given(/^I touch on "([^"]*)"$/) do |text|
  touch "TextView text:'#{text}'"
end

Then(/^I scroll until I see the "([^"]*)"$/) do |text|
  q = query("TextView text:'#{text}'")
  while q.empty?
    scroll_down
    q = query("TextView text:'#{text}'")
  end
end

Then(/^I confirm that the file has not been downloaded$/) do
  wait_for(timeout: 60) {0 == query("ImageView tag:'open'").length}
  #sleep(5)
end

Then(/^I uncheckBox checkbox "([^"]*)"$/) do |arg|
  touch "CheckBox id:'checkBoxSyncOnLoad'"
end

Then(/^I sleep for (\d+) seconds$/) do |arg|
  #sleep(5)
end