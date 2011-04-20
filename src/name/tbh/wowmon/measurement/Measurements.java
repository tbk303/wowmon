package name.tbh.wowmon.measurement;

public interface Measurements<T> extends Iterable<T> {

	public abstract void addMeasurement(T value);

	public abstract T getCurrentValue();

	public abstract T getMin();

	public abstract T getMax();

	public abstract T getAvg();

	public abstract void reset();

	public abstract String getName();

	public abstract int count();

	public abstract T[] getData();

	public abstract int maxCount();
}