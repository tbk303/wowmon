package name.tbh.wowmon.measurement;

import java.util.Iterator;

/**
 * Value ringbuffer providing min, max, avg and an iterator.
 */
public abstract class AbstractMeasurements<T> implements Measurements<T> {

	private String name;
	protected T[] values;
	protected T min;
	protected T max;
	protected T sum;
	protected int numMeasurements;
	protected int latestMeasurement = -1;

	public AbstractMeasurements(final String name, int bufferSize) {
		this.name = name;
		values = createBuffer(bufferSize);
	}

	@Override
	public int count() {
		return Math.min(numMeasurements, latestMeasurement + 1);
	}

	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.tbh.wowmon.measurement.Measurements#addMeasurement(T)
	 */
	@Override
	public void addMeasurement(T value) {
		latestMeasurement = (latestMeasurement + 1) % values.length;
		values[latestMeasurement] = value;
		numMeasurements++;
		updateMinMaxSum(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.tbh.wowmon.measurement.Measurements#getCurrentValue()
	 */
	@Override
	public T getCurrentValue() {
		if (latestMeasurement < 0) {
			return null;
		}
		return values[latestMeasurement];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.tbh.wowmon.measurement.Measurements#getMin()
	 */
	@Override
	public T getMin() {
		return min;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.tbh.wowmon.measurement.Measurements#getMax()
	 */
	@Override
	public T getMax() {
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.tbh.wowmon.measurement.Measurements#getAvg()
	 */
	@Override
	public abstract T getAvg();

	public int getBufferSize() {
		return values.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see name.tbh.wowmon.measurement.Measurements#reset()
	 */
	@Override
	public void reset() {
		min = max = sum = null;
		numMeasurements = 0;
		values = createBuffer(values.length);
	}

	protected abstract T[] createBuffer(int bufferSize);

	protected abstract void updateMinMaxSum(T value);

	@Override
	public T[] getData() {
		return values.clone();
	}

	@Override
	public int maxCount() {
		return getBufferSize();
	}

	/**
	 * Provides the value buffer as an Iterator; the last value is the latest
	 * measured value.
	 */
	public Iterator<T> iterator() {
		final T[] iteratorBuf = values.clone();
		final int latest = latestMeasurement;
		return new Iterator<T>() {
			private int current = latest;
			private int iterated;

			@Override
			public boolean hasNext() {
				return iterated < iteratorBuf.length;
			}

			@Override
			public T next() {
				current = (current + 1) % iteratorBuf.length;
				T val = iteratorBuf[current];
				iterated++;
				return val;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}
}