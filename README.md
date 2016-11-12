README
---------------------------
minSDK: 14
Android Version: Work on Android 4.3 or higher

Changes since first submit:
	1) working landscape views for all activities
	2) working current location, automatically set to your current location (however you can select the current location box to change your current location from this default if it isnt accurate enough)
	3) fixed formatting on contact view, made everything more consistent 
	4) fixed "choose destination" button did not link to new page
	5) created a list of emergency contacts that can be set in settings that will automatically be added to your selected contacts on each trip you take
	6) alert plays alert tone instead of ring tone
	7) error handling if radio buttons left unchecked


Before starting the app:
	-If using Genymotion, make sure to add some contacts with valid
	phone numbers otherwise contact list will be empty, also turn on GPS
	-If using android device, make sure to turn on your location provider,
	if its not on the app will present a dialogue with a link to the page
	where you can turn it on


1) App starts with instructions on how to use app
2) Valid Addresses:
	3700 North Charles Street
	3201 Guilford Avenue Baltimore
	*Can put under eiher location/destination
3) Settings:
	Alert Notification:
		Ringtone will play when the timer runs out at default: 0 minutes
		Choose "X min" to have ringtone play when there are X minutes left on timer
	Notification Sound
		Ringtone is default ringtone of phone
		Choose a ringtone from list in case the sound is "None"
	Alert Message
		Can customize if you wish
4)Go through app....
5)RINGTONE:
	When ringtone sounds, open app again to turn it off.