## How to setup oculus for development ##

As a developer you have two options. Extend the source code from SVN, or hook into the command server. The benefit of the [CommandServer](http://code.google.com/p/oculus/source/browse/trunk/oculus/WEB-INF/src/developer/CommandServer.java) is that you can get updates without having to merge your code. Also, because it is just a simple socket connection, you can use any language you prefer. So, C, ruby, python etc is all going to be supported this way. The last benefit is that you can connect to robot without logging into the web based control screens. Basic 'zombie mode' requires a valid user name and password, but you are not going to show up as a full fledged user in the web app controls.

commands are available, but will be extended as there is need.

To enable this feature, you must set:

```
commandport 4444
```

in your [Oculus/conf/oculus\_settings.txt](http://code.google.com/p/oculus/wiki/Oculus_Settings_File)

This will turn on the [Command Server](http://code.google.com/p/oculus/source/browse/trunk/oculus/WEB-INF/src/developer/CommandServer.java) and take most of the [PlayerCommands](http://code.google.com/p/oculus/source/browse/trunk/oculus/WEB-INF/src/oculus/PlayerCommands.java).

| command | description |
|:--------|:------------|
| _**tail**_ | returns the end of the current log file |
| _**find**_ | request a 'dockgrab' command and return the coordinates for the dock. |
| _**dock**_ | initiate a docking |
| _**undock**_ | back to bot away from the doc |
| _**nudge**_ | left|right|backward|forward  |
| _**move**_ | left|right|backward|forward  |
| _**reboot**_ | restarts the oculus web application |
| _**publish**_ | turn on the video stream so you can do _find_ commands |
| _**state**_ | dump current variables in the [State](http://code.google.com/p/oculus/source/browse/trunk/oculus/WEB-INF/src/oculus/State.java) object |
| _**state xxx yyy**_ | will set variable xxx to the value of yyy in the [State Objects](http://code.google.com/p/oculus/source/browse/trunk/oculus/WEB-INF/src/oculus/State.java) |
| _**tcp**_ | returns the number of active TCP Connections |
| _**users**_ | returns the number of active web users |
| _**factory**_ | restore factory settings |
| _**restart**_ | restarts the host computer |
| _**reboot**_ | restarts the oculus web application |
| _**version**_ | gets the current version of the robot's software. |
| _**settings**_ | dumps all the settings from the config file. _will not send email password._ |


The Terminal can be launched from the command line, shell script or right from eclipse. You can use and valid user, pass word pair in plain text, or the hashed value. You can use telnet and then login with user:pass combo at the prompt.

```
telnet 192.168.1.176 4444 
Connected to 192.168.1.176.
Escape character is '^]'.
oculus version 438 ready for login.
user:pass 
```

```
java -classpath "./webapps/oculus/WEB-INF/classes/" developer.terminal.Terminal 192.168.1.176 4444 brad password
```

Or like this using the hashed password.

```
java developer.terminal.Terminal 192.168.1.176 4444 brad +tVfDvTicIG5chk8ibFky34L63A8= & 
```

The parameters are, robot's local ip, port number, user name, hashed password. (find these in oculus/conf/oculus\_settings.txt)

This is available in plain text over TCP, but you client must first send user name and password first. As an example of how the TCP connections are sent updates, see below. We can chat with the logged in users and any TCP connections as well. All tcp commands will be seen in the web user's screens as chat text for now, so you''ll see the requests the clients are making.

If you extend from AbstractTerminal, you can avoid dealing with just the State object and the out going stream object. The changes you make in these classes actually run on your workstation you code/test on, so no need to re-deploy and restart the server. Another reason for using the CommandServer is faster development.

<br> You simply send commands and watch for changes in state ans seen in this example:<br>
<br>
<a href='http://code.google.com/p/oculus/source/browse/trunk/oculus/WEB-INF/src/developer/terminal/FindHome.java'>http://code.google.com/p/oculus/source/browse/trunk/oculus/WEB-INF/src/developer/terminal/FindHome.java</a>

An Example in screen capture below:<br>
<br />
<a href='http://www.youtube.com/watch?feature=player_embedded&v=-JDNDlVpnhk' target='_blank'><img src='http://img.youtube.com/vi/-JDNDlVpnhk/0.jpg' width='425' height=344 /></a><br>
<br />
<i>watch in 480p if you want to be able to actually read the text on screen</i>
<br />

<h2>Deploying class and java script files</h2>

If you are coding directly in java, here is deployment script that will move your files over to the robot. The use of 'sudo' and 'chown' are required to make SMB shares behave.<br>
<br>
<pre><code>#!/bin/sh<br>
<br>
LOCAL="/Users/brad/Documents/workspace/Oculus/webapps/oculus"<br>
REMOTE="/Volumes/temp/oculus/webapps/oculus"<br>
<br>
sudo rm -rf $REMOTE/*.html<br>
sudo rm -rf $REMOTE/javascript<br>
sudo rm -rf $REMOTE/WEB-INF/classes<br>
sudo rm -rf $REMOTE/WEB-INF/src<br>
#sudo rm -rf $REMOTE/flash<br>
<br>
sudo cp $LOCAL/*.html $REMOTE/<br>
sudo cp -r $LOCAL/javascript $REMOTE/<br>
sudo cp -r $LOCAL/WEB-INF/classes $REMOTE/WEB-INF/classes/<br>
sudo cp -r $LOCAL/WEB-INF/src $REMOTE/WEB-INF/<br>
#sudo cp -r $LOCAL/flash $REMOTE/<br>
        <br>
sudo chown -R brad $REMOTE<br>
date<br>
</code></pre>

<h3>Samba not working? Use curl or ftp</h3>

If running an FTP server on the robot, you can use this script to deploy your class files. This is useful if your robot is not on your LAN and you still need to update and restart the server. (note: this uses the same user, password pair for FTP and oculus terminal.)<br>
<br>
<pre><code>#!/bin/sh<br>
<br>
# don't put your passwords in scripts<br>
PASSWD=$1<br>
HOST='192.168.1.176'<br>
PORT='4444'<br>
USER='brad'<br>
<br>
function doFolder {<br>
	echo $1<br>
	for f in $1/*.class<br>
	do<br>
		curl -T "$f" ftp://$HOST/$1 --user $USER:$PASSWD --ftp-create-dirs<br>
	done<br>
}<br>
<br>
doFolder 'webapps/Oculus/WEB-INF/classes/oculus/'<br>
doFolder 'webapps/Oculus/WEB-INF/classes/oculus/commport/'<br>
doFolder 'webapps/Oculus/WEB-INF/classes/developer/'<br>
doFolder 'webapps/Oculus/WEB-INF/classes/developer/terminal/'<br>
doFolder 'webapps/Oculus/WEB-INF/classes/developer/sonar/'<br>
doFolder 'webapps/Oculus/WEB-INF/classes/developer/ftp/'<br>
<br>
# change if script file not in root <br>
CLASSPATH='webapps/oculus/WEB-INF/classes/'<br>
<br>
# reboot oculus<br>
java -classpath $CLASSPATH developer.terminal.Terminal $HOST $PORT $USER $PASSWD restart bye<br>
</code></pre>

Note that you can open a terminal session and then send a few commands. In this case, restart the server and then logout. This basically uploads the changes and gets the server back up.<br>
<br>
<br />
