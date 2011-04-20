package name.tbh.wowmon.measurement;

public class LongMeasurements extends AbstractMeasurements<Long> {

	public LongMeasurements(final String name, int bufferSize) {
		super(name, bufferSize);
	}

	@Override
	public Long getAvg() {
		return numMeasurements > 0 ? Math.round((1d * sum) / numMeasurements) : null;
	}

	@Override
	protected Long[] createBuffer(int bufferSize) {
		return new Long[bufferSize];
	}

	@Override
	protected void updateMinMaxSum(Long value) {
		if (value == null) {
			return;
		}
		if (min == null || value < min)
			min = value;
		if (max == null || value > max)
			max = value;
		if (sum == null)
			sum = 0L;
		sum += value;
	}
}
