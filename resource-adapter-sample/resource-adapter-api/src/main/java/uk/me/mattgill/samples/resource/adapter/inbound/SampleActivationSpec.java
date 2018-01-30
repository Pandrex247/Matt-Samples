package uk.me.mattgill.samples.resource.adapter.inbound;

import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import uk.me.mattgill.samples.resource.adapter.api.SampleListener;

@Activation(messageListeners = SampleListener.class)
public class SampleActivationSpec implements ActivationSpec {

    private ResourceAdapter adapter;

	@ConfigProperty(type = String.class, description = "The topic to subscribe the MDB to")
	private String topic;

	@Override
	public ResourceAdapter getResourceAdapter() {
        return adapter;
	}

	@Override
	public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
		this.adapter = ra;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public void validate() throws InvalidPropertyException {
		if (topic == null || topic.isEmpty()) {
            throw new InvalidPropertyException("The topic can't be null");
        }
	}

}