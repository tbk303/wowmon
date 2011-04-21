package name.tbh.wowmon.sensor;

public class DisconnectedException extends RuntimeException {

	private static final long serialVersionUID = -8440373921359342417L;

	public DisconnectedException(final Throwable cause) {
		super(cause);
	}
}
