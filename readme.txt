*******************
** NOISE MONITOR **
*******************

Targeted Features
*******************
v. 1.0:
- Call/SMS Phone when threshold has been hit
- Send Alert when battery power is low
- Babyphone mode which puts the phone's ringer mode to silent while the service is running

v. 1.1
- Allow Phone number selection over contacts
- Visualize mic amplitude

v. 1.5:
- Add a telephone signal coverage monitor which notifies the user if he has lost the telephone signal

v. 2.0:
- Offer live monitoring via server

ISSUES:
*******************
- 0.9.3: Media Server uses a lot of battery

BUGS:
*******************
- notification are sometimes updated even after the monitor was stopped
- test phone call with distance to mic

TODO for v. 1.0:
*******************
- *DONE* resume monitor thread if alert was unsuccessful 
- *DONE* update notification after alert has been executed
- *DONE* implement baby phone mode
- *DONE* implement SMS alert
- *DONE* write text which explains the application and it's settings in more detail
- *DONE* adjust noise threshold formula
- *DONE* java doc
- *DONE* cleanup GUI interface
- *DONE* cleanup code
- *DONE* check that phone has reception before starting monitor
- *DONE* Send Alert when battery power is low -> needs testing on real phone (ACTION_BATTERY_LOW is somehow never send in emulator??)
- testing + bug fixing
- publish

TODO for v 1.1:
******************
- Allow Phone number selection from contacts
- Visualize mic amplitude (possible solution: http://smus.com/android-phonegap-plugins)