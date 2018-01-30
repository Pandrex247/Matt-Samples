package uk.me.mattgill.samples.resource.adapter.outbound;

import java.io.Serializable;

import javax.resource.ResourceException;

import uk.me.mattgill.samples.resource.adapter.api.SampleConnection;

public class SampleConnectionImpl implements SampleConnection {

	private SampleManagedConnection realConnection;

	SampleConnectionImpl(SampleManagedConnection conn) {
		realConnection = conn;
	}

	@Override
	public void close() throws Exception {
		realConnection.removeHandle(this);
	}

	public void setRealConnection(SampleManagedConnection realConnection) {
		this.realConnection = realConnection;
	}

	@Override
	public void publish(String topic, Serializable message, int qos) throws ResourceException {
		realConnection.publish(topic, message, qos);
	}

	@Override
	public void publish(String topic, Serializable message) throws ResourceException {
		publish(topic, message, 0);
	}

}