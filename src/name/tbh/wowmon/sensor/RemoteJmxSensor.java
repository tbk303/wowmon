package name.tbh.wowmon.sensor;

import javax.management.openmbean.CompositeData;

import name.tbh.wowmon.measurement.DoubleMeasurements;
import name.tbh.wowmon.measurement.LongMeasurements;
import name.tbh.wowmon.measurement.Measurements;

public class RemoteJmxSensor implements Sensor {

	private JmxSession session;
	private String objectName;
	private String attributeName;
	private Measurements<?> measurements;
	private String compositeFieldName;

	public RemoteJmxSensor(JmxSession session, String objectName, String attributeName, Measurements<?> measurements) {
		this(session, objectName, attributeName, null, measurements);
	}

	public RemoteJmxSensor(JmxSession session, String objectName, String attributeName, String compositeFieldName,
			Measurements<?> measurements) {
		this.session = session;
		this.objectName = objectName;
		this.attributeName = attributeName;
		this.measurements = measurements;
		this.compositeFieldName = compositeFieldName;
	}

	/* (non-Javadoc)
	 * @see name.tbh.wowmon.sensor.Sensor#update()
	 */
	@Override
	public synchronized void update() {
		Object value = session.readObject(objectName, attributeName);
		if (value instanceof CompositeData && compositeFieldName != null) {
			final CompositeData composite = (CompositeData) value;
			value = composite.get(compositeFieldName);
		}

		if (measurements instanceof LongMeasurements) {
			Long longValue = null;
			if (value instanceof Integer) {
				longValue = (long) ((Integer) value).longValue();
			} else if (value instanceof Long) {
				longValue = (Long) value;
			}
			((LongMeasurements) measurements).addMeasurement(longValue);
		} else if (measurements instanceof DoubleMeasurements) {
			((DoubleMeasurements) measurements).addMeasurement((Double) value);
		}
	}
}
