package name.tbh.wowmon.sensor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientLogger implements Sensor {

	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private JmxSession session;
	private final Set<String> knownClients = new HashSet<String>();
	private final Writer writer;

	public ClientLogger(final JmxSession session, final String path) throws IOException {
		this.session = session;

		FileWriter fstream = new FileWriter(path);
		writer = new BufferedWriter(fstream);
	}

	@Override
	public void update() {
		//		final List<String> hallClients = session
		//				.readStringAttributes(
		//						"WowzaMediaServerPro:applicationInstanceName=mainhall,applicationInstances=ApplicationInstances,applicationName=*,applications=Applications,clientId=*,clients=Clients,name=Client,vHostName=_defaultVHost_,vHosts=VHosts",
		//						"ip");
		//		final List<String> seminarClients = session
		//				.readStringAttributes(
		//						"WowzaMediaServerPro:applicationInstanceName=seminar,applicationInstances=ApplicationInstances,applicationName=*,applications=Applications,clientId=*,clients=Clients,name=Client,vHostName=_defaultVHost_,vHosts=VHosts",
		//						"ip");
		final List<String> setupClients = session
				.readStringAttributes(
						"WowzaMediaServerPro:applicationInstanceName=aufbau,applicationInstances=ApplicationInstances,applicationName=*,applications=Applications,clientId=*,clients=Clients,name=Client,vHostName=_defaultVHost_,vHosts=VHosts",
						"ip");

		final List<String> currentClients = new ArrayList<String>(/*hallClients.size()
																	+ seminarClients.size() +*/setupClients.size());

		currentClients.addAll(setupClients);
		//		currentClients.addAll(seminarClients);
		//		currentClients.addAll(hallClients);

		for (final String client : currentClients) {
			if (knownClients.add(client)) {
				try {
					writer.write(FORMAT.format(Calendar.getInstance().getTime()) + " - " + client + "\n");
					writer.flush();
				} catch (final IOException e) {
					System.err.println("Error writing log: " + e);
				}
			}
		}
	}
}
