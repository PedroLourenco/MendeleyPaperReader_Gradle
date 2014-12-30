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
  "true" == query("CheckBox id:'checkBoxSyncOnLoad'",:isChecked)

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
  sleep(5)
end

Then(/^I uncheckBox checkbox "([^"]*)"$/) do |arg|
  touch "CheckBox id:'checkBoxSyncOnLoad'"
end

Then(/^I sleep for (\d+) seconds$/) do |arg|
  sleep(5)
end