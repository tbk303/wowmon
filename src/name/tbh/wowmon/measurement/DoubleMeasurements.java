package name.tbh.wowmon.measurement;

public class DoubleMeasurements extends AbstractMeasurements<Double> {

	public DoubleMeasurements(final String name, int bufferSize) {
		super(name, bufferSize);
	}

	@Override
	public Double getAvg() {
		return numMeasurements > 0 ? sum / numMeasurements : null;
	}

	@Override
	protected Double[] createBuffer(int bufferSize) {
		return new Double[bufferSize];
	}

	@Override
	protected void updateMinMaxSum(Double value) {
		if (value == null) {
			return;
		}
		if (min == null || value < min)
			min = value;
		if (max == null || value > max)
			max = value;
		if (sum == null)
			sum = 0d;
		sum += value;
	}

}
