package name.tbh.wowmon.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import name.tbh.wowmon.common.Util;
import name.tbh.wowmon.measurement.DoubleMeasurements;
import name.tbh.wowmon.measurement.LongMeasurements;
import name.tbh.wowmon.measurement.Measurements;
import name.tbh.wowmon.sensor.ClientLogger;
import name.tbh.wowmon.sensor.JmxSession;
import name.tbh.wowmon.sensor.JmxSessionCredentials;
import name.tbh.wowmon.sensor.RemoteCpuUsageJmxSensor;
import name.tbh.wowmon.sensor.RemoteJmxSensor;
import name.tbh.wowmon.sensor.Sensor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

public class Wowmon extends Composite {

	// Update interval in milliseconds
	public static final int INTERVAL = 1000;
	// IP logging interval in milliseconds
	public static final int LOG_INTERVAL = 10000;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	private final Text url;
	private final Text user;
	private final Button connect;
	private final Button disconnect;
	private final Text password;
	private final Label status;

	private final Collection<SensorWidget<?>> widgets = new ArrayList<SensorWidget<?>>();
	private final Collection<Sensor> sensors = new ArrayList<Sensor>();

	private boolean connected = false;
	private Composite statistics;

	private String logPath;

	private LongMeasurements connTotal;
	private LongMeasurements connTotalRtmp;
	private LongMeasurements connTotalHttp;
	private LongMeasurements connHall;
	private LongMeasurements connHallRtmp;
	private LongMeasurements connHallHttp;
	private LongMeasurements connSeminar;
	private LongMeasurements connSeminarRtmp;
	private LongMeasurements connSeminarHttp;
	private DoubleMeasurements bytesTotal;
	private DoubleMeasurements bytesTotalRtmp;
	private DoubleMeasurements bytesTotalHttp;
	private DoubleMeasurements bytesHall;
	private DoubleMeasurements bytesHallRtmp;
	private DoubleMeasurements bytesHallHttp;
	private DoubleMeasurements bytesSeminar;
	private DoubleMeasurements bytesSeminarRtmp;
	private DoubleMeasurements bytesSeminarHttp;

	private DoubleMeasurements systemLoad;
	private LongMeasurements systemMemory;
	private LongMeasurements systemThreads;

	private static final class MbitSensorWidget extends SensorWidget<Double> {
		private MbitSensorWidget(Composite parent, Measurements<Double> total, Measurements<Double>... parts) {
			super(parent, total, parts);
		}

		@Override
		protected String format(final Double value) {
			return value == null ? "" : String.format("%.2f", value * 8.0 / 1024.0 / 1000.0);
		}

		@Override
		protected double value(final Double value) {
			return super.value(value * 8.0 / 1024.0 / 1000.0);
		}
	}

	private static final class MByteSensorWidget extends SensorWidget<Long> {
		private MByteSensorWidget(Composite parent, Measurements<Long> total, Measurements<Long>... parts) {
			super(parent, total, parts);
		}

		@Override
		protected String format(final Long value) {
			return value == null ? "" : String.format("%.2f", value / 1024.0 / 1000.0);
		}

		@Override
		protected double value(final Long value) {
			return value / 1024.0 / 1000.0;
		}
	}

	private static class SimpleListener implements SelectionListener {

		@Override
		public void widgetSelected(SelectionEvent e) {
			// Do nothing by default
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

	}

	public Wowmon(final Composite parent) {
		this(parent, SWT.NONE);
	}

	public Wowmon(final Composite parent, final int style) {
		super(parent, style);

		setLayout(new GridLayout(1, true));

		final Group session = new Group(this, SWT.BORDER);
		session.setText("Connection");
		session.setLayout(new GridLayout(4, false));
		session.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		statistics = new Composite(this, SWT.NONE);
		statistics.setLayout(new GridLayout(3, false));
		statistics.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		connTotal = new LongMeasurements("Total connections", 50);
		connTotalRtmp = new LongMeasurements("Total connections (RTMP)", 50);
		connTotalHttp = new LongMeasurements("Total connections (HTTP)", 50);

		connHall = new LongMeasurements("Hall connections", 50);
		connHallRtmp = new LongMeasurements("Hall connections (RTMP)", 50);
		connHallHttp = new LongMeasurements("Hall connections (HTTP)", 50);

		connSeminar = new LongMeasurements("Seminar connections", 50);
		connSeminarRtmp = new LongMeasurements("Seminar connections (RTMP)", 50);
		connSeminarHttp = new LongMeasurements("Seminar connections (HTTP)", 50);

		bytesTotal = new DoubleMeasurements("Total Mbit/s OUT", 50);
		bytesTotalRtmp = new DoubleMeasurements("Total Mbit/s OUT (RTMP)", 50);
		bytesTotalHttp = new DoubleMeasurements("Total Mbit/s OUT (HTTP)", 50);

		bytesHall = new DoubleMeasurements("Hall Mbit/s OUT", 50);
		bytesHallRtmp = new DoubleMeasurements("Hall Mbit/s OUT (RTMP)", 50);
		bytesHallHttp = new DoubleMeasurements("Hall Mbit/s OUT (HTTP)", 50);

		bytesSeminar = new DoubleMeasurements("Seminar Mbit/s OUT", 50);
		bytesSeminarRtmp = new DoubleMeasurements("Seminar Mbit/s OUT (RTMP)", 50);
		bytesSeminarHttp = new DoubleMeasurements("Seminar Mbit/s OUT (HTTP)", 50);

		systemLoad = new DoubleMeasurements("CPU utilization in %", 50);
		systemMemory = new LongMeasurements("Memory usage in MB", 50);
		systemThreads = new LongMeasurements("Thread count", 50);

		@SuppressWarnings("unchecked")
		final SensorWidget<Long> connectionsTotal = new SensorWidget<Long>(statistics, connTotal, connTotalHttp,
				connTotalRtmp);
		widgets.add(connectionsTotal);

		@SuppressWarnings("unchecked")
		final SensorWidget<Long> connectionsHall = new SensorWidget<Long>(statistics, connHall, connHallHttp,
				connHallRtmp);
		widgets.add(connectionsHall);

		@SuppressWarnings("unchecked")
		final SensorWidget<Long> connectionsSeminar = new SensorWidget<Long>(statistics, connSeminar, connSeminarHttp,
				connSeminarRtmp);
		widgets.add(connectionsSeminar);

		@SuppressWarnings("unchecked")
		final SensorWidget<Double> bytesOutTotal = new MbitSensorWidget(statistics, bytesTotal, bytesTotalHttp,
				bytesTotalRtmp);
		widgets.add(bytesOutTotal);

		@SuppressWarnings("unchecked")
		final SensorWidget<Double> bytesOutHall = new MbitSensorWidget(statistics, bytesHall, bytesHallHttp,
				bytesHallRtmp);
		widgets.add(bytesOutHall);

		@SuppressWarnings("unchecked")
		final SensorWidget<Double> bytesOutSeminar = new MbitSensorWidget(statistics, bytesSeminar, bytesSeminarHttp,
				bytesSeminarRtmp);
		widgets.add(bytesOutSeminar);

		final SensorWidget<Double> sysLoad = new SensorWidget<Double>(statistics, systemLoad) {
			@Override
			protected String format(final Double value) {
				return value == null ? "" : String.format("%.2f", value);
			}
		};
		widgets.add(sysLoad);
		final MByteSensorWidget sysMemory = new MByteSensorWidget(statistics, systemMemory);
		widgets.add(sysMemory);
		final SensorWidget<Long> sysThreads = new SensorWidget<Long>(statistics, systemThreads);
		widgets.add(sysThreads);

		statistics.pack();

		status = new Label(this, SWT.BORDER);
		status.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		status.setText("Disconnected");

		url = createLabelledText(session, "URL");
		url.setLayoutData(new GridData(350, SWT.DEFAULT));
		user = createLabelledText(session, "User");
		password = createLabelledText(session, "Password");
		password.setEchoChar('*');

		final Composite buttons = new Composite(session, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));

		connect = new Button(buttons, SWT.NONE);
		connect.setText("Connect");

		disconnect = new Button(buttons, SWT.NONE);
		disconnect.setText("Disconnect");
		disconnect.setEnabled(false);

		connect.addSelectionListener(new SimpleListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
				fd.setText("Select log file");
				fd.setFilterExtensions(new String[] { "*.log" });
				logPath = fd.open();

				new Thread() {
					@Override
					public void run() {
						Wowmon.this.getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								doConnect(url.getText(), user.getText(), password.getText());
							}
						});
					}
				}.start();
			}
		});

		disconnect.addSelectionListener(new SimpleListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setConnectedState(false);
				status.setText("Disconnected");
			}
		});

	}

	protected Text createLabelledText(final Composite parent, final String label) {

		final Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(2, false));

		Label l = new Label(g, SWT.LEFT);
		l.setText(label);
		Text t = new Text(g, SWT.BORDER | SWT.SINGLE);
		t.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return t;
	}

	protected void doConnect(final String url, final String user, final String password) {
		status.setText("Connecting...");

		JmxSessionCredentials credentials = new JmxSessionCredentials(url, Util.nullIfEmpty(user),
				Util.nullIfEmpty(password));
		JmxSession session = new JmxSession(credentials);

		boolean success = true;

		try {
			session.connect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			errorDialog("Invalid URL", "Invalid JMX URL\n\n" + e);
			success = false;
		} catch (IOException e) {
			e.printStackTrace();
			errorDialog("I/O error", "Error establishing connection\n\n" + e);
			success = false;
		} catch (SecurityException e) {
			e.printStackTrace();
			errorDialog("Invalid authentication", "Wrong credentials\n\n" + e);
			success = false;
		}

		setConnectedState(success);

		if (success) {
			final ClientLogger logger;

			try {
				logger = new ClientLogger(session, logPath);
			} catch (final IOException e) {
				e.printStackTrace();
				errorDialog("Log path error", "Error opening log file\n\n" + e);
				setConnectedState(false);
				return;
			}

			status.setText("Connected");

			initSensors(session);

			new Thread() {
				public void run() {
					while (Wowmon.this.connected) {
						try {
							logger.update();
							Thread.sleep(LOG_INTERVAL);
						} catch (InterruptedException e) {
							// NOTHING
						}
					}
				};
			}.start();

			new Thread() {
				public void run() {
					while (Wowmon.this.connected && !Wowmon.this.isDisposed()) {
						final Date date = new Date();
						long nextUpdate = System.currentTimeMillis() + INTERVAL;
						updateSensorValues();
						if (!Wowmon.this.isDisposed()) {
							getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									status.setText("Connected - last update " + DATE_FORMAT.format(date));
									refreshSensorDisplay();
									statistics.pack();
									Wowmon.this.update();
								}
							});
						}
						long yetToSleep = nextUpdate - System.currentTimeMillis();
						if (yetToSleep > 0L) {
							try {
								Thread.sleep(yetToSleep);
							} catch (InterruptedException e) {
								// EMPTY
							}
						}
					}
				}
			}.start();
		} else {
			status.setText("Disconnected");
		}
	}

	private void initSensors(final JmxSession session) {
		sensors.clear();

		sensors.add(new RemoteJmxSensor(session, "WowzaMediaServerPro:name=Connections", "current", connTotal));
		sensors.add(new RemoteJmxSensor(session, "WowzaMediaServerPro:name=ConnectionsRTMP", "current", connTotalRtmp));
		sensors.add(new RemoteJmxSensor(session, "WowzaMediaServerPro:name=ConnectionsHTTPCupertino", "current",
				connTotalHttp));

		sensors.add(new RemoteJmxSensor(session, "WowzaMediaServerPro:name=IOPerformance", "messagesOutBytesRate",
				bytesTotal));
		sensors.add(new RemoteJmxSensor(session, "WowzaMediaServerPro:name=IOPerformanceRTMP", "messagesOutBytesRate",
				bytesTotalRtmp));
		sensors.add(new RemoteJmxSensor(session, "WowzaMediaServerPro:name=IOPerformanceHTTPCupertino",
				"messagesOutBytesRate", bytesTotalHttp));

		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=mainhall,name=Connections",
		//				"current", connHall));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=mainhall,name=ConnectionsRTMP",
		//				"current", connHallRtmp));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=mainhall,name=ConnectionsHTTPCupertino",
		//				"current", connHallHttp));
		//
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=mainhall,name=IOPerformance",
		//				"messagesOutBytesRate", bytesHall));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=mainhall,name=IOPerformanceRTMP",
		//				"messagesOutBytesRate", bytesHallRtmp));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=mainhall,name=IOPerformanceHTTPCupertino",
		//				"messagesOutBytesRate", bytesHallHttp));

		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=seminar,name=Connections",
		//				"current", connSeminar));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=seminar,name=ConnectionsRTMP",
		//				"current", connSeminarRtmp));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=seminar,name=ConnectionsHTTPCupertino",
		//				"current", connSeminarHttp));
		//
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=seminar,name=IOPerformance",
		//				"messagesOutBytesRate", bytesSeminar));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=seminar,name=IOPerformanceRTMP",
		//				"messagesOutBytesRate", bytesSeminarRtmp));
		//		sensors.add(new RemoteJmxSensor(
		//				session,
		//				"WowzaMediaServerPro:vHosts=VHosts,vHostName=_defaultVHost_,applications=Applications,applicationName=live,applicationInstances=ApplicationInstances,applicationInstanceName=seminar,name=IOPerformanceHTTPCupertino",
		//				"messagesOutBytesRate", bytesSeminarHttp));

		sensors.add(new RemoteCpuUsageJmxSensor(session, systemLoad));
		sensors.add(new RemoteJmxSensor(session, "java.lang:type=Memory", "HeapMemoryUsage", "used", systemMemory));
		sensors.add(new RemoteJmxSensor(session, "java.lang:type=Threading", "ThreadCount", systemThreads));
	}

	protected void updateSensorValues() {
		for (final Sensor sensor : sensors) {
			sensor.update();
		}
	}

	protected void refreshSensorDisplay() {
		for (final SensorWidget<?> widget : widgets) {
			widget.refresh();
		}
	}

	protected void setConnectedState(final boolean state) {

		connected = state;

		disconnect.setEnabled(state);
		url.setEnabled(!state);
		user.setEnabled(!state);
		password.setEnabled(!state);
		connect.setEnabled(!state);
	}

	protected void errorDialog(final String title, final String message) {
		final MessageBox box = new MessageBox(this.getShell(), SWT.APPLICATION_MODAL | SWT.OK);
		box.setText(title);
		box.setMessage(message);
		box.open();
	}
}
