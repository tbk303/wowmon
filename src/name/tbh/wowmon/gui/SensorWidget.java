package name.tbh.wowmon.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import name.tbh.wowmon.measurement.Measurements;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

public class SensorWidget<T> extends Composite {

	private final Measurements<T> totals;
	private final Map<String, MeasurementsDisplay<T>> parts = new LinkedHashMap<String, MeasurementsDisplay<T>>();

	private final ChartComposite chart;

	private final Label total;
	private final Label totalMin;
	private final Label totalMax;
	private final Label totalAvg;

	private static final class MeasurementsDisplay<T> {
		private final Measurements<T> measurements;
		private final Label label;
		private final Label labelMin;
		private final Label labelMax;
		private final Label labelAvg;

		public MeasurementsDisplay(final Measurements<T> m, final Label l, final Label lmin, final Label lmax,
				final Label lavg) {
			measurements = m;
			label = l;
			labelMin = lmin;
			labelMax = lmax;
			labelAvg = lavg;
		}
	}

	public SensorWidget(final Composite parent, final Measurements<T> total, final Measurements<T>... parts) {
		this(parent, SWT.NONE, total, parts);
	}

	public SensorWidget(final Composite parent, final int style, final Measurements<T> totals,
			final Measurements<T>... parts) {
		super(parent, style);

		this.totals = totals;

		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TimeTableXYDataset dataset = new TimeTableXYDataset();
		XYBarRenderer renderer = new StackedXYBarRenderer(0.0);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);

		if (parts == null || parts.length == 0) {
			renderer.setSeriesPaint(0, Color.green);
		}

		XYPlot plot = new XYPlot(dataset, new DateAxis("Time"), new NumberAxis("Count"), renderer);

		plot.getDomainAxis().setLowerMargin(0.0);
		plot.getDomainAxis().setUpperMargin(0.0);
		plot.getRangeAxis().setLabel(null);
		plot.getDomainAxis().setLabel(null);

		JFreeChart realChart = new JFreeChart(plot);
		realChart.setTitle((String) null);

		chart = new ChartComposite(this, SWT.NONE, realChart, true);
		chart.setLayoutData(new GridData(300, 180));

		final Composite labels = new Composite(this, SWT.NONE);
		labels.setLayout(new GridLayout(5, false));

		final Label totalLabel = new Label(labels, SWT.NONE);
		totalLabel.setText(totals.getName());

		total = new Label(labels, SWT.BOLD);
		final FontData[] fd = total.getFont().getFontData();
		fd[0].setStyle(SWT.BOLD);
		total.setFont(new Font(getDisplay(), fd));
		totalMin = new Label(labels, SWT.NONE);
		totalMax = new Label(labels, SWT.NONE);
		totalAvg = new Label(labels, SWT.NONE);

		if (parts != null && parts.length > 0) {
			for (Measurements<T> part : parts) {
				final Label label = new Label(labels, SWT.NONE);
				label.setText(part.getName());
				final Label valueLabel = new Label(labels, SWT.BOLD);
				valueLabel.setFont(new Font(getDisplay(), fd));
				this.parts.put(part.getName(), new MeasurementsDisplay<T>(part, valueLabel,
						new Label(labels, SWT.NONE), new Label(labels, SWT.NONE), new Label(labels, SWT.NONE)));
			}
		}
	}

	public void refresh() {
		total.setText(format(totals.getCurrentValue()));
		totalMin.setText("(" + format(totals.getMin()));
		totalMax.setText(" " + format(totals.getMax()) + " ");
		totalAvg.setText(format(totals.getAvg()) + ")");

		final XYPlot plot = (XYPlot) chart.getChart().getPlot();
		final TimeTableXYDataset dataset = (TimeTableXYDataset) plot.getDataset();
		dataset.clear();

		final Date now = new Date();

		if (parts.isEmpty()) {
			fillDataset(dataset, totals, now);
		} else {
			for (final MeasurementsDisplay<T> m : parts.values()) {
				final Measurements<T> measurements = m.measurements;
				fillDataset(dataset, measurements, now);
				m.label.setText(format(m.measurements.getCurrentValue()));
				m.labelMin.setText("(" + format(m.measurements.getMin()));
				m.labelMax.setText(" " + format(m.measurements.getMax()) + " ");
				m.labelAvg.setText(format(m.measurements.getAvg()) + ")");
			}
		}
	}

	private void fillDataset(final TimeTableXYDataset dataset, final Measurements<T> measurements, final Date now) {

		final List<Double> data = new ArrayList<Double>();
		for (final T raw : measurements) {
			data.add(raw == null ? 0.0 : value(raw));
		}

		long current = now.getTime();

		for (final Double value : data) {

			Date start = new Date(current);
			current += Wowmon.INTERVAL;
			Date end = new Date(current);

			TimePeriod period = new SimpleTimePeriod(start, end);

			dataset.add(period, value, measurements.getName(), true);
		}
	}

	protected String format(final T value) {
		return value == null ? "" : value.toString();
	}

	protected double value(final T value) {
		if (value instanceof Integer) {
			return ((Integer) value).doubleValue();
		}
		if (value instanceof Long) {
			return ((Long) value).doubleValue();
		}
		if (value instanceof Double) {
			return ((Double) value).doubleValue();
		}
		if (value instanceof Float) {
			return ((Float) value).doubleValue();
		}

		throw new UnsupportedOperationException("Please implement proper conversion!");
	}
}
