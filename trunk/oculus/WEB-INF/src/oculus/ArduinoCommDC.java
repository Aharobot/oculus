package oculus;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

public class ArduinoCommDC implements SerialPortEventListener {

	private Logger log = Red5LoggerFactory.getLogger(ArduinoCommDC.class, "oculus");

	// shared state variables
	private State state = State.getReference();

	// if watchdog'n, re-connect if not seen input since this long ago
	public static final long DEAD_TIME_OUT = 30000;
	public static final int SETUP = 2000;
	public static final int SONAR_DELAY = 3000; 
	public static final int WATCHDOG_DELAY = 5000;

	// this commands require arguments from current state
	public static final byte FORWARD = 'f';
	public static final byte BACKWARD = 'b';
	public static final byte LEFT = 'l';
	public static final byte RIGHT = 'r';
	public static final byte COMP = 'c';
	public static final byte CAM = 'v';
	public static final byte ECHO = 'e';

	// ready to be sent
	public static final byte[] SONAR = { 'd' };
	public static final byte[] STOP = { 's' };
	public static final byte[] GET_VERSION = { 'y' };
	public static final byte[] CAMRELEASE = { 'w' };
	private static final byte[] ECHO_ON = { 'e', '1' };
	private static final byte[] ECHO_OFF = { 'e', '0' };

	// comm cannel
	private SerialPort serialPort = null;
	private InputStream in;
	private OutputStream out;

	// will be discovered from the device
	protected String version = null;

	// input buffer
	private byte[] buffer = new byte[32];
	private int buffSize = 0;

	// track write times
	private long lastSent = System.currentTimeMillis();
	private long lastRead = System.currentTimeMillis();

	Settings settings = new Settings();
	protected int speedslow = settings.getInteger("speedslow");
	protected int speedmed = settings.getInteger("speedmed");
	protected int camservohoriz = settings.getInteger("camservohoriz");
	protected int camposmax = settings.getInteger("camposmax");
	protected int camposmin = settings.getInteger("camposmin");
	protected int nudgedelay = settings.getInteger("nudgedelay");
	protected int maxclicknudgedelay = settings.getInteger("maxclicknudgedelay");
	protected int maxclickcam = settings.getInteger("maxclickcam");
	protected double clicknudgemomentummult = settings.getDouble("clicknudgemomentummult");
	protected int steeringcomp = settings.getInteger("steeringcomp");
	protected final boolean sonar = settings.getBoolean(State.sonarenabled);

	protected int camservodirection = 0;
	protected int camservopos = camservohoriz;
	protected int camwait = 400;
	protected int camdelay = 50; // for smooth continuous motion
	protected int speedfast = 255;
	protected int turnspeed = 255;
	protected int speed = speedfast; // set default to max
	
	protected String direction = null;
	public boolean moving = false;
	volatile boolean sliding = false;
	public volatile boolean movingforward = false;
	
	// TODO: what are these ?? can't be in methods instead? 
	int tempspeed = 999;
	int clicknudgedelay = 0;
	String tempstring = null;
	int tempint = 0;

	// make sure all threads know if connected
	private volatile boolean isconnected = false;

	// call back
	private Application application = null;

	/**
	 * Constructor but call connect to configure
	 * 
	 * @param app
	 *            is the main oculus application, we need to call it on Serial
	 *            events like restet
	 */
	public ArduinoCommDC(Application app) {

		// call back to notify on reset events etc
		application = app;

		if (state.get(State.serialport) != null) {
			new Thread(new Runnable() {
				public void run() {

					connect();
					Util.delay(SETUP);
					new Sender(GET_VERSION);
					camHoriz();

					// check for lost connection
					new WatchDog().start();

				}
			}).start();
		}
	}

	/** open port, enable read and write, enable events */
	public void connect() {
		try {

			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(
					state.get(State.serialport)).open(
					ArduinoCommDC.class.getName(), SETUP);
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open streams
			out = serialPort.getOutputStream();
			in = serialPort.getInputStream();

			// register for serial events
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

		} catch (Exception e) {
			log.error(e.getMessage());
			return;
		}
	}

	/** @return True if the serial port is open */
	public boolean isConnected() {
		return isconnected;
	}

	@Override
	/** 
	 * buffer input on event and trigger parse on '>' charter  
	 * 
	 * Note, all feedback must be in single xml tags like: <feedback 123>
	 */
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				byte[] input = new byte[32];
				int read = in.read(input);
				for (int j = 0; j < read; j++) {
					// print() or println() from arduino code
					if ((input[j] == '>') || (input[j] == 13)
							|| (input[j] == 10)) {
						// do what ever is in buffer
						if (buffSize > 0)
							execute();
						// reset
						buffSize = 0;
						// track input from arduino
						lastRead = System.currentTimeMillis();
					} else if (input[j] == '<') {
						// start of message
						buffSize = 0;
					} else {
						// buffer until ready to parse
						buffer[buffSize++] = input[j];
					}
				}
			} catch (IOException e) {
				log.error("event : " + e.getMessage());
			}
		}
	}

	// act on feedback from arduino
	private void execute() {
		String response = "";
		for (int i = 0; i < buffSize; i++)
			response += (char) buffer[i];
		
		// System.out.println("in: " + response);

		// take action as arduino has just turned on
		if (response.equals("reset")) {

			// might have new firmware after reseting
			isconnected = true;
			version = null;
			new Sender(GET_VERSION);
			updateSteeringComp();

		} else if (response.startsWith("version:")) {
			if (version == null) {
				// get just the number
				version = response.substring(response.indexOf("version:") + 8, response.length());
				application.message("arduinoculus version: " + version, null, null);
			} else return;

			// don't bother showing watch dog pings to user screen
		} else if (response.charAt(0) != GET_VERSION[0]) {

			// if sonar enabled will get <cm xxx> as watchdog
			if (response.startsWith("cm")) {
				final String val = response.substring(response.indexOf(' ') + 1, response.length());
				final int range = Integer.parseInt(val);				
				if (Math.abs(range - state.getInteger(State.sonardistance)) > 1) {
					state.set(State.sonardistance, val);
					if(state.getBoolean(State.sonardebug))
						application.message("sonar range: " + range, null, null);
				}
				
				// must be an echo 
			} else application.message("arduinoculus: " + response, getReadDelta()+"ms", getWriteDelta()+"ms");
		}
	}

	/** @return the time since last write() operation */
	public long getWriteDelta() {
		return System.currentTimeMillis() - lastSent;
	}

	/** @return this device's firmware version */
	public String getVersion() {
		return version;
	}

	/** @return the time since last read operation */
	public long getReadDelta() {
		return System.currentTimeMillis() - lastRead;
	}

	/** inner class to send commands as a seperate thread each */
	private class Sender extends Thread {
		private byte[] command = null;
		public Sender(final byte[] cmd) {
			// do connection check
			if(!isconnected) log.error("not connected");
			else {
				command = cmd;
				this.start();
			}
		}
		public void run() {
			sendCommand(command);
		}
	}

	/**
	 * @param update
	 *            is set to true to turn on echo'ing of serial commands
	 */
	public void setEcho(boolean update) {
		if (update) new Sender(ECHO_ON);
		else new Sender(ECHO_OFF);
	}

	/** inner class to check if getting responses in timely manor */
	private class WatchDog extends Thread {
		public WatchDog() {
			this.setDaemon(true);
		}
		public void run() {
			Util.delay(SETUP);
			while (true) {
				
				if (getReadDelta() > DEAD_TIME_OUT) {
					log.error("arduino watchdog time out");
					return; // die, no point living 
				}
				
				// TODO: COLIN ... should we re-connect? 
				// if ( !isconnected) { connect(); return; }
				
				if (sonar){ 
					if (getReadDelta() > SONAR_DELAY){ 
						new Sender(SONAR);
						Util.delay(SONAR_DELAY);						
					}
				} else {
					if (getReadDelta() > WATCHDOG_DELAY){
						new Sender(GET_VERSION);
						Util.delay(WATCHDOG_DELAY);
					}
				}
			}
		}
	}

	public void reset() {
		if (isconnected) {
			new Thread(new Runnable() {
				public void run() {
					disconnect();
					connect();
				}
			}).start();
		}
	}

	/** shutdown serial port */
	protected void disconnect() {
		try {
			in.close();
			out.close();
			isconnected = false;
			version = null;
		} catch (Exception e) {
			log.error("disconnect(): " + e.getMessage());
		}
		serialPort.close();
	}

	/**
	 * Send a multi byte command to send the arduino
	 * 
	 * @param command
	 *            is a byte array of messages to send
	 */
	private synchronized void sendCommand(final byte[] command) {

		if (!isconnected)
			return;

		try {

			// send
			out.write(command);

			// end of command
			out.write(13);

		} catch (Exception e) {
			reset();
			log.error(e.getMessage());
		}

		// track last write
		lastSent = System.currentTimeMillis();
	}

	/** */
	public void stopGoing() {
		
		if (application.muteROVonMove && moving) { application.unmuteROVMic(); }
		
		new Sender(STOP);
		moving = false;
		movingforward = false;
					
	}

	/** */
	public void goForward() {
		new Sender(new byte[] { FORWARD, (byte) speed });
		moving = true;
		movingforward = true;
		
		if (application.muteROVonMove) { application.muteROVMic(); }
	}

	/** */
	public void pollSensor() {
		if (sonar) new Sender(SONAR);
	}

	/** */
	public void goBackward() {
		new Sender(new byte[] { BACKWARD, (byte) speed });
		moving = true;
		movingforward = false;
		
		if (application.muteROVonMove) { application.muteROVMic(); }
	}

	/** */
	public void turnRight() {
		int tmpspeed = turnspeed;
		int boost = 10;
		if (speed < turnspeed && (speed + boost) < speedfast)
			tmpspeed = speed + boost;

		new Sender(new byte[] { RIGHT, (byte) tmpspeed });
		moving = true;
		
		if (application.muteROVonMove) { application.muteROVMic(); }
	}

	/** */
	public void turnLeft() {
		int tmpspeed = turnspeed;
		int boost = 10;
		if (speed < turnspeed && (speed + boost) < speedfast)
			tmpspeed = speed + boost;

		new Sender(new byte[] { LEFT, (byte) tmpspeed });
		moving = true;
		
		if (application.muteROVonMove) { application.muteROVMic(); }
	}

	public void camGo() {
		new Thread(new Runnable() {
			public void run() {
				while (camservodirection != 0) {
					sendCommand(new byte[] { CAM, (byte) camservopos });
					Util.delay(camdelay);
					camservopos += camservodirection;
					if (camservopos > camposmax) {
						camservopos = camposmax;
						camservodirection = 0;
					}
					if (camservopos < camposmin) {
						camservopos = camposmin;
						camservodirection = 0;
					}
				}
				Util.delay(250);
				if(!state.getBoolean(State.holdservo))
					sendCommand(CAMRELEASE);
			}
		}).start();
	}

	public void camCommand(String str) {
		if (str.equals("stop")) {
			camservodirection = 0;
		} else if (str.equals("up")) {
			camservodirection = 1;
			camGo();
		} else if (str.equals("down")) {
			camservodirection = -1;
			camGo();
		} else if (str.equals("horiz")) {
			camHoriz();
		} else if (str.equals("downabit")) {
			camservopos -= 5;
			if (camservopos < camposmin) {
				camservopos = camposmin;
			}
			new Thread(new Runnable() {
				public void run() {
					sendCommand(new byte[] { CAM, (byte) camservopos });
					Util.delay(camdelay);
					if(!state.getBoolean(State.holdservo))
						sendCommand(CAMRELEASE);
				}
			}).start();
		} else if (str.equals("upabit")) {
			camservopos += 5;
			if (camservopos > camposmax) {
				camservopos = camposmax;
			}
			new Thread(new Runnable() {
				public void run() {
					sendCommand(new byte[] { CAM, (byte) camservopos });
					Util.delay(camwait);
					if(!state.getBoolean(State.holdservo))
						sendCommand(CAMRELEASE);
				}
			}).start();
		}
	}

	/** level the camera servo */
	public void camHoriz() {
		camservopos = camservohoriz;
		new Thread(new Runnable() {
			public void run() {
				try {
					byte[] cam = { CAM, (byte) camservopos };
					sendCommand(cam);
					Thread.sleep(camwait);
					if(!state.getBoolean(State.holdservo))
						sendCommand(CAMRELEASE);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void camToPos(Integer n) {
		camservopos = n;
		new Thread(new Runnable() {
			public void run() {
				try {
					sendCommand(new byte[] { CAM, (byte) camservopos });
					Thread.sleep(camwait);
					if(!state.getBoolean(State.holdservo))
						sendCommand(CAMRELEASE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/** Set the speed on the bot */
	public void speedset(String str) {
		if (str.equals("slow")) {
			speed = speedslow;
		}
		if (str.equals("med")) {
			speed = speedmed;
		}
		if (str.equals("fast")) {
			speed = speedfast;
		}
		if (movingforward) {
			goForward();
		}
	}

	public void nudge(String dir) {
		direction = dir;
		new Thread(new Runnable() {
			public void run() {
				int n = nudgedelay;
				if (direction.equals("right")) {
					turnRight();
				}
				if (direction.equals("left")) {
					turnLeft();
				}
				if (direction.equals("forward")) {
					goForward();
					movingforward = false;
					n *= 4;
				}
				if (direction.equals("backward")) {
					goBackward();
					n *= 4;
				}
		
				Util.delay(n);
		
				if (movingforward == true) {
					goForward();
				} else {
					stopGoing();
				}
			}
		}).start();
	}

	public void slide(String dir) {
		if (sliding == false) {
			sliding = true;
			direction = dir;
			tempspeed = 999;
			new Thread(new Runnable() {
				public void run() {
					try {
						int distance = 300;
						int turntime = 500;
						tempspeed = speed;
						speed = speedfast;
						if (direction.equals("right")) {
							turnLeft();
						} else {
							turnRight();
						}
						Thread.sleep(turntime);
						if (sliding == true) {
							goBackward();
							Thread.sleep(distance);
							if (sliding == true) {
								if (direction.equals("right")) {
									turnRight();
								} else {
									turnLeft();
								}
								Thread.sleep(turntime);
								if (sliding == true) {
									goForward();
									Thread.sleep(distance);
									if (sliding == true) {
										stopGoing();
										sliding = false;
										speed = tempspeed;
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public void slidecancel() {
		if (sliding == true) {
			if (tempspeed != 999) {
				speed = tempspeed;
				sliding = false;
			}
		}
	}

	public Integer clickSteer(String str) {
		tempstring = str;
		tempint = 999;
		String xy[] = tempstring.split(" ");
		if (Integer.parseInt(xy[1]) != 0) {
			tempint = clickCam(Integer.parseInt(xy[1]));
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					String xy[] = tempstring.split(" ");
					if (Integer.parseInt(xy[0]) != 0) {
						if (Integer.parseInt(xy[1]) != 0) {
							Thread.sleep(camwait);
						}
						clickNudge(Integer.parseInt(xy[0]));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return tempint;
	}

	public void clickNudge(Integer x) {
		if (x > 0) {
			direction = "right";
		} else {
			direction = "left";
		}
		clicknudgedelay = maxclicknudgedelay * (Math.abs(x)) / 320;
		/*
		 * multiply clicknudgedelay by multiplier multiplier increases to
		 * CONST(eg 2) as x approaches 0, 1 as approaches 320
		 * ((320-Math.abs(x))/320)*1+1
		 */
		double mult = Math.pow(((320.0 - (Math.abs(x))) / 320.0), 3)
				* clicknudgemomentummult + 1.0;
		// System.out.println("clicknudgedelay-before: "+clicknudgedelay);
		clicknudgedelay = (int) (clicknudgedelay * mult);
		// System.out.println("n: "+clicknudgemomentummult+" mult: "+mult+" clicknudgedelay-after: "+clicknudgedelay);
		new Thread(new Runnable() {
			public void run() {
				try {
					tempspeed = speed;
					speed = speedfast;
					if (direction.equals("right")) {
						turnRight();
					} else {
						turnLeft();
					}
					Thread.sleep(clicknudgedelay);
					speed = tempspeed;
					if (movingforward == true) {
						goForward();
					} else {
						stopGoing();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public Integer clickCam(Integer y) {
		Integer n = maxclickcam * y / 240;
		camservopos -= n;
		if (camservopos > camposmax) {
			camservopos = camposmax;
		}
		if (camservopos < camposmin) {
			camservopos = camposmin;
		}

		new Thread(new Runnable() {
			public void run() {
				try {
					byte[] command = { CAM, (byte) camservopos };
					sendCommand(command);
					Thread.sleep(camwait + clicknudgedelay);
					if(!state.getBoolean(State.holdservo))
						sendCommand(CAMRELEASE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return camservopos;
	}
	
	/** turn off the servo holding the mirror */ 
	public void releaseCameraServo(){
		new Thread(new Runnable() {
			public void run() {
				try {
					sendCommand(CAMRELEASE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/** send steering compensation values to the arduino */
	public void updateSteeringComp() {
		byte[] command = { COMP, (byte) steeringcomp };
		new Sender(command);
	}
}
