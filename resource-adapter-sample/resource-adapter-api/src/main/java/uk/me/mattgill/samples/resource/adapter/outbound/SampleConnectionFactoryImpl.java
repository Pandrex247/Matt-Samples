package uk.me.mattgill.samples.resource.adapter.outbound;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import uk.me.mattgill.samples.resource.adapter.api.SampleConnection;
import uk.me.mattgill.samples.resource.adapter.api.SampleConnectionFactory;

public class SampleConnectionFactoryImpl implements SampleConnectionFactory {

	private Logger logger = Logger.getLogger(SampleConnectionFactoryImpl.class.getName());

	private SampleManagedConnectionFactory cf;
	private ConnectionManager cm;

	SampleConnectionFactoryImpl(SampleManagedConnectionFactory cf, ConnectionManager cm) {
		this.cf = cf;
		this.cm = cm;
	}

	@Override
	public SampleConnection getConnection() {
		try {
			if (cm != null) {
				return (SampleConnection) cm.allocateConnection(cf, null);
			} else {
				return (SampleConnection) cm.allocateConnection(null, null);
			}
		} catch (ResourceException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		return null;
	}

}