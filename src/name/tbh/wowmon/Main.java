package name.tbh.wowmon;

import name.tbh.wowmon.gui.Wowmon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class Main {

	private static final String VERSION = "0.1";

	public static void main(String[] args) {

		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);

		shell.setText("Wowmon " + VERSION);
		shell.setLayout(new FillLayout());

		new Wowmon(shell);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
