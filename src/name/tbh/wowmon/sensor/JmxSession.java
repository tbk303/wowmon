package name.tbh.wowmon.sensor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import name.tbh.wowmon.common.Util;

public class JmxSession {
	private JmxSessionCredentials credentials;
	private JMXServiceURL url;
	private JMXConnector connector;
	private MBeanServerConnection connection;

	public JmxSession(JmxSessionCredentials credentials) {
		this.credentials = credentials;
	}

	public void connect() throws IOException {
		url = new JMXServiceURL(credentials.getUrl());
		if (Util.isEmpty(credentials.getUsername())) {
			connector = JMXConnectorFactory.connect(url);
		} else {
			Map<String, Object> envMap = new HashMap<String, Object>();
			envMap.put(
					"jmx.remote.credentials",
					new String[] { credentials.getUsername(),
							credentials.getPassword() });
			envMap.put(Context.SECURITY_PRINCIPAL, credentials.getUsername());
			envMap.put(Context.SECURITY_CREDENTIALS, credentials.getPassword());
			connector = JMXConnectorFactory.connect(url, envMap);
		}
		connection = connector.getMBeanServerConnection();
	}

	public Object readObject(String name, String attributeName) {
		try {
			return connection.getAttribute(new ObjectName(name), attributeName);
		} catch (Exception e) {
			System.err.println("Warning: Error querying attribute \""
					+ attributeName + "\" in \"" + name + "\": " + e);
			return null;
		}
	}
}
