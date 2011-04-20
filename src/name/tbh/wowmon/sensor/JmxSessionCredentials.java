package name.tbh.wowmon.sensor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.tbh.wowmon.common.Util;

public class JmxSessionCredentials {
	private final static String serializationSeparator = "######";
	private String url;
	private String username;
	private String password;

	public JmxSessionCredentials(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public static JmxSessionCredentials fromString(String s) {
		String[] comps = s.split(serializationSeparator);
		return new JmxSessionCredentials(comps[0], Util.nullIfEmpty(comps[1]),
				Util.nullIfEmpty(comps[2]));
	}

	@Override
	public String toString() {
		return url + serializationSeparator
				+ (username == null ? "" : username) + serializationSeparator
				+ (password == null ? "" : password);
	}

	public String toReadableString() {
		StringBuilder buf = new StringBuilder();
		if (!Util.isEmpty(username)) {
			buf.append(username).append("@");
		}
		// service:jmx:rmi://revision.scene.org:8084/jndi/rmi://revision.scene.org:8085/jmxrmi
		Pattern urlPattern = Pattern.compile("//([^:/]+)");
		Matcher m = urlPattern.matcher(url);
		if (m.find()) {
			buf.append(m.group(1));
		} else {
			buf.append(url);
		}
		return buf.toString();
	}
}
