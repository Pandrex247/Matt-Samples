package uk.me.mattgill.samples.resource.adapter.outbound;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import uk.me.mattgill.samples.resource.adapter.api.SampleConnection;
import uk.me.mattgill.samples.resource.adapter.api.SampleConnectionFactory;

@ConnectionDefinition(
	connection = SampleConnection.class,
	connectionImpl = SampleConnectionImpl.class,
	connectionFactory = SampleConnectionFactory.class,
	connectionFactoryImpl = SampleConnectionFactoryImpl.class
)
public class SampleManagedConnectionFactory implements ManagedConnectionFactory, Serializable {

	private PrintWriter logWriter;

	@Override
	public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
		return new SampleConnectionFactoryImpl(this, cxManager);
	}

	@Override
	public Object createConnectionFactory() throws ResourceException {
		return createConnectionFactory(null);
	}

	@Override
	public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
			throws ResourceException {
		return new SampleManagedConnection(this, cxRequestInfo);
	}

	@Override
	public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
			ConnectionRequestInfo cxRequestInfo) throws ResourceException {
		return (ManagedConnection) connectionSet.toArray()[0];
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