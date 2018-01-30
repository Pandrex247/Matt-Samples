package uk.me.mattgill.samples.resource.adapter.inbound;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

@Connector(
    displayName = "Sample Resource Adapter",
    vendorName = "Matt Gill",
    version = "1.0-SNAPSHOT"
)
public class SampleResourceAdapter implements ResourceAdapter {

    private final Logger logger = Logger.getLogger(SampleResourceAdapter.class.getName());

    private BootstrapContext ctx;

    private final Map<MessageEndpointFactory, SampleSubscriber> registeredFactories;

    public SampleResourceAdapter() {
        registeredFactories = new HashMap<>();
    }

	@Override
	public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
		this.ctx = ctx;
	}

	@Override
	public void stop() {
		for (SampleSubscriber sub : registeredFactories.values()) {
            try {
                sub.close();
            } catch (ResourceException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
	}

	@Override
	public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
			throws ResourceException {
        SampleSubscriber sub = new SampleSubscriber(endpointFactory, spec, ctx.getWorkManager());
        registeredFactories.put(endpointFactory, sub);
        sub.subscribe();
	}

	@Override
	public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        SampleSubscriber sub = registeredFactories.get(endpointFactory);
        if (sub != null) {
            try {
                sub.close();
            } catch (ResourceException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        registeredFactories.remove(endpointFactory);
	}

	@Override
	public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
		return null;
	}

}