THE oculus\_settings.txt CONFIG FILE

The oculus\_settings.txt file is located under /Oculus/conf/, and contains all settings pertaining to Oculus. This file is generally maintained by the application but a few advanced settings are changed manually. It is split into 3 sections: 'GUI Settings,' 'Manual Settings,' and 'User List.'
GUI settings are set within the application itself, as is the user list. Each line represents a single value, with the setting name, followed by a space, then the value.
If the file is deleted, or 'restore factory settings' is selected from initialize.html, it is re-generated with default values, on application restart.
<br>

GUI SETTINGS<br>
<br>
<b>skipsetup</b> <i>yes | no</i>
indicates whether the server application starts by launching initialize.html, or server.html. After initial setup, typically server.html is always launched<br>
<br>
<b>speedslow</b> <i>{INT}</i>
(0-255) PWM value used by ArduinOculus to drive the wheel motors at slow speed<br>
<br>
<b>speedmed</b> <i>{INT}</i>
(0-255) PWM value used by ArduinOculus to drive the wheel motors at medium speed. (speedfast is always 255)<br>
<br>
<b>steeringcomp</b> <i>{INT}</i>
(0-255) used to set wheel motors steering compensation, 128 is no compensation<br>
<br>
<b>camservohoriz</b> <i>{INT}</i>
(0-255) mirror tilt servo position at horizontal<br>
<br>
<b>camposmax</b> <i>{INT}</i>
<i>(0-255) maximum mirror tilt servo position</i>

<b>camposmin</b> <i>{INT}</i>
<i>(0-255) minimum mirror tilt servo position</i>

<b>nudgedelay</b> <i>{INT}</i>
time in milliseconds for wheel motors nudge move<br>
<br>
<b>docktarget</b> <i>{STRING}</i>
dock calibration proportions/metrics<br>
<br>
<b>vidctroffset</b> <i>{INT}</i>
manual dock line offset from center, in pixels<br>
<br>
<b>vlow</b> <i>{INT}</i>
low video settings width, height, fps, and bandwidth(0-100)<br>
<br>
<b>vmed</b> <i>{INT}</i>
medium video settings width, height, fps, and bandwidth(0-100)<br>
<br>
<b>vhigh</b> <i>{INT}</i>
high video settings width, height, fps, and bandwidth(0-100)<br>
<br>
<b>vfull</b> <i>{INT}</i>
full video settings width, height, fps, and bandwidth(0-100)<br>
<br>
<b>vcustom</b> <i>{INT}</i>
custom video settings width, height, fps, and bandwidth(0-100)<br>
<br>
<b>vset</b> <i>{STRING}</i>
current video setting<br>
<br>
<b>maxclicknudgedelay</b> <i>{INT}</i>
Clicksteer time in milliseconds for wheels to shift video image horizontally from the center to the very left or right edge of the screen<br>
<br>
<b>clicknudgemomentummult</b> <i>{DOUBLE}</i>
Momentum multiplier affecting horizontal clicksteer accuracy<br>
<br>
<b>maxclickcam</b> <i>{INT}</i>
vertical (mirror tilt) clicksteer setting<br>
<br>
<b>muteonrovmove</b> <i>true | false</i>
mutes robot mic on wheel motor movement<br>
<br>
<b>videoscale</b> <i>{INT}</i>
scale of web browser client video<br>
<br>
<b>volume</b> <i>{INT}</i>
(percent) system volume<br>
<br>
<b>holdservo</b> <i>true | false</i>
mirror tilt servo power brakes<br>
<br>
<b>loginnotify</b> <i>true | false</i>
controls voice synthesizer announcement of user login<br>
<br>
<b>reboot</b> <i>true | false</i>
set to reboot laptop every 48 hours (Windows only)<br>
<br>
<b>selfmicpushtotalk</b> <i>true | false</i>
enable/disable push 'T' to un-mute self mic in web browser client<br>
<br>

MANUAL SETTINGS<br>
<br>
<b>email_smtp_server</b> {STRING}<br>
SMTP server for outgoing email. 'Disabled' if unused<br>
<br>
<b>email_smtp_port</b> {STRING}<br>
SMTP server port for outgoing email<br>
<br>
<b>email_username</b> {STRING}<br>
username if SMTP server for outgoing email requires authorization. 'Disabled' if unused<br>
<br>
<b>email_password</b> {STRING}<br>
password if SMTP server for outgoing email requires authorization. 'Disabled' if unused. WARNING: PLAIN TEXT<br>
<br>
<b>email_from_address</b> {STRING}<br>
return email address for outgoing email. 'Disabled' if unused<br>
<br>
<b>developer</b> <i>true | false</i>
enable (alpha) developer features<br>
<br>
<b>debugenabled</b> <i>true | false</i>
enable verbose logging<br>
<br>
<b>commandport</b> {INT}<br>
TCP port for socket client connection. 'Disabled' if unused<br>
<br>
<b>stopdelay</b> {INT}<br>
time in milliseconds for wheels to come to full stop from max speed<br>
<br>
<b>vself</b> {INT}<br>
web browser client self video settings width, height, fps, and bandwidth(0-100)<br>
<br>
<b>arduinoculus</b> {STRING}<br>
ArduinOculus microcontroller serial port. Set to 'discovery' for auto-detection, 'disabled,' or port name<br>
<br>
<b>oculed</b> {STRING}<br>
OcuLED Lights serial port. Set to 'discovery' for auto-detection, 'disabled,' or port name<br>
<br>

USER LIST<br>
<br>
<b>salt</b> {STRING}<br>
randomly generated key used in password encryption<br>
<br>
<b>user0</b> {STRING}<br>
admin username<br>
<br>
<b>pass0</b> {STRING}<br>
admin password, encrypted<br>
<br>
<b>user1..2..3..</b> {STRING}<br>
additional usernames (non admin)<br>
<br>
<b>pass1..2..3..</b> {STRING}<br>
additional passwords, encrypted (non admin)