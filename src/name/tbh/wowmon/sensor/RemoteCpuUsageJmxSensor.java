package name.tbh.wowmon.sensor;

import name.tbh.wowmon.measurement.Measurements;

/**
 * Calculates the CPU usage as percentage in the same way as JConsole does.
 * 
 * @author Timo B. Huebel (t.h@gmx.info)
 * 
 */
public class RemoteCpuUsageJmxSensor implements Sensor {

	private JmxSession session;
	private Measurements<Double> measurements;

	long prevUptime = -1;
	long prevCputime = -1;

	public RemoteCpuUsageJmxSensor(JmxSession session, Measurements<Double> measurements) {
		this.session = session;
		this.measurements = measurements;
	}

	@Override
	public synchronized void update() {
		int cpus = (Integer) session.readObject("java.lang:type=OperatingSystem", "AvailableProcessors");
		long uptime = (Long) session.readObject("java.lang:type=Runtime", "Uptime");
		long cputime = (Long) session.readObject("java.lang:type=OperatingSystem", "ProcessCpuTime");

		if (prevUptime > 0 && uptime > prevUptime) {
			final long elapsedCpu = cputime - prevCputime;
			final long elapsedTime = uptime - prevUptime;
			final double javacpu = Math.min(99F, elapsedCpu / (elapsedTime * 10000F * cpus));
			measurements.addMeasurement(javacpu);
		} else {
			measurements.addMeasurement(0.001);
		}

		prevUptime = uptime;
		prevCputime = cputime;
	}
}
