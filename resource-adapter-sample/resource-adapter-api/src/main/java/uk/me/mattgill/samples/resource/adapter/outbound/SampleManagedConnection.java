package uk.me.mattgill.samples.resource.adapter.outbound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import javax.ws.rs.NotSupportedException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import uk.me.mattgill.samples.resource.adapter.api.SampleConnection;

public class SampleManagedConnection implements ManagedConnection, SampleConnection {

	private final SampleManagedConnectionFactory cf;
	private PrintWriter logWriter;
	private final Set<ConnectionEventListener> listeners;
	private final List<SampleConnection> connectionHandles = new LinkedList<>();
	private final MqttClient client;

	SampleManagedConnection(SampleManagedConnectionFactory cf, ConnectionRequestInfo requestInfo)
			throws ResourceException {
		this.cf = cf;
		this.listeners = new HashSet<>();
		String clientId = MqttClient.generateClientId();
		try {
			client = new MqttClient("tcp://localhost:1883", clientId);

			MqttConnectOptions options = new MqttConnectOptions();
			options.setServerURIs(new String[] { "tcp://test.mosquitto.org:1883" });

			client.connect(options);
		} catch (MqttException ex) {
			throw new ResourceException("Unable to build connection", ex);
		}
	}

	@Override
	public void close() throws Exception {
		destroy();
	}

	@Override
	public void publish(String topic, Serializable message, int qos) throws ResourceException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutput objectStream = new ObjectOutputStream(outputStream);
			objectStream.writeObject(message);
			objectStream.flush();
			byte[] messageContents = outputStream.toByteArray();
			client.publish(topic, messageContents, qos, true);
			objectStream.close();
			outputStream.close();
		} catch (MqttException ex) {
			throw new ResourceException("Unable to send message to server.", ex);
		} catch (IOException ex) {
			throw new ResourceException("Unable to create message to send.", ex);
		}
	}

	@Override
	public void publish(String topic, Serializable message) throws ResourceException {
		publish(topic, message, 0);
	}

	@Override
	public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
		SampleConnection conn = new SampleConnectionImpl(this);
		connectionHandles.add(conn);
		return conn;
	}

	@Override
	public void destroy() throws ResourceException {
		try {
			client.disconnect();
			client.close();
		} catch (MqttException ex) {
			throw new ResourceException("Unable to close client connection", ex);
		}
	}

	@Override
	public void cleanup() throws ResourceException {
		connectionHandles.clear();
	}

	@Override
	public void associateConnection(Object connection) throws ResourceException {
		SampleConnectionImpl conn = (SampleConnectionImpl) connectionHandles;
		conn.setRealConnection(this);
		connectionHandles.add(conn);
	}

	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public XAResource getXAResource() throws ResourceException {
		throw new NotSupportedException("XA Resources not supported");
	}

	@Override
	public LocalTransaction getLocalTransaction() throws ResourceException {
		throw new NotSupportedException("Local transactions not supported");
	}

	@Override
	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		return new ManagedConnectionMetaData() {
			@Override
			public String getUserName() throws ResourceException {
				return "anon";
			}

			@Override
			public int getMaxConnections() throws ResourceException {
				return 0;
			}

			@Override
			public String getEISProductVersion() throws ResourceException {
				return "1.0-SNAPSHOT";
			}

			@Override
			public String getEISProductName() throws ResourceException {
				return "Sample JCA Adapter";
			}
		};
	}

	public void removeHandle(SampleConnectionImpl conn) {
		connectionHandles.remove(conn);
		for (ConnectionEventListener listener : listeners) {
			listener.connectionClosed(new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED));
		}
	}

	@Override
	public void setLogWriter(PrintWriter out) throws ResourceException {
		this.logWriter = out;
	}

	@Override
	public PrintWriter getLogWriter() throws ResourceException {
		return logWriter;
	}

}