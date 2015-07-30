Todo: (>=5 after demo units)

4-rudimentary help (build website/wiki with full help, include local copy of that with software)

4-auto-dock: disable if camera off, don't allow camera change when docking, disable movement commands, etc.

4.9-auto dock: once allowed 2x overlapping docking routines! havoc.

4-autodock - add scale to 320 for calibrate

4-get rid of error when in initialize.html , and client calls publish (respond warning "no video")

4-grabber status still says wrong user when passenger login etc

4-software autoupdate--update webapps/oculus and oculus\_settings.txt


6-disable 'undock' button when not docked

6-server sofware doesn't work under 'Program Files'

6-server quit doesn't work under folders with spaces...

6-red5 1.0rc1 -- find workaround for external jars from old red5

6-autodock choose initial threshhold based on imgaverag ratio

6-get rid of facegrab?

6-only disable clicksteer if privacy panel showing (?)-look at "Security.showSettings(SecurityPanel.PRIVACY)". Euchred? see: http://www.actionscript.org/forums/showthread.php3?t=194656

6-make oculus.log persistent/cummulative

6-disable remote access to 'initialize.html' and 'server.html' (only sort of disabled)

7-video off after dock-?

6 - fix self streaming window closes when hitting cam/mic off

5.5- some kind of meaningful signal strength indicator or graph

5-add status: localstream

5-fix timestamp on server messages (<10 min have no '0')

5-screen on (wake) --  automate?

6-add system command: restart red5/eeedroidRed5

5-add cyclops up-time, client connect up-time

5-test with WPA/WEP etc.--WPA OK, WEP unknown (old)

5-add 'execute system command' feature

5-sound quality settings

5-e-brake for hill stop?

5-tilt servo released unless driving?  So far not necessary

5-retrace tracks to last ping

6-wifi signal strength combine with retraced steps for lost wifi mapping?

5-wake from sleep, wake on lan, action?

5-autodetect suitable browsers

5-android app

5-iPhone app

6-video lag calc

5-ask permission to hijack admin user, or have admin user setting 'allow hijack y/n'

7-keyboard enable/disable setting

5-add solution to recover from flash crash (need browser restart etc)

5.5-eliminate errors when no arduinoattached

6-overlay doesn't work in IE6

8-call serial port disconnect on 'quit'--?
  * Maybe use the java shutdown hook (if not seeing serial data coming in?)


5-auto-stop after 2? minutes if no movement commands received

5-firmware, if (partial)? reset, stop motion (anti-stalling) -- 3rd post on this page http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1281729580 AND/OR add software arduino reset command (but that won't work because its not responding to commands...)
  * also should solve in firmware and serialPort (if not getting data in, basic counter, then disconnect from port and connect again to cause a reset)

6-firmware auto update

6-add 'doubledock' option, for units like eeepc701 where it needs to dock twice in quick succession to register charging

6-anti 'miss-dock' intelligence: if docked and suddenly starts draining, auto-jostle position

6-lost dock--if jostled or lost charge while idle, jostle/creep forward to re-engage, if fail send email etc.

5-still need relaunch grabber on windows7 (if goes to sleep?)
> -check log--appDisconnect is supposed to send 'grabber disconnected' message
> -try: on 'appDisconnect' => disconnect any passengers, then relaunch grabber [auto-relaunch, see if helps with this not sure](added.md)

6-move clicksteer on/off to settings.txt

6-self cam, more settings (mic only, resolution, etc.)

6-use dock to auto-set camera tilt limits

6-use dock to auto-set steering comp(?)

6-use dock to auto-set clicksteer(?)

6.5-more complete passenger info (message on login/logout, complete list of passengers on client(s) and server, etc.)