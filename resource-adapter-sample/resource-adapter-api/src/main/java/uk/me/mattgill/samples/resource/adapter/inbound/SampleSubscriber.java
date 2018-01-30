package uk.me.mattgill.samples.resource.adapter.inbound;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SampleSubscriber implements IMqttMessageListener {

    private MessageEndpointFactory ef;
    private MqttClient client;
    private WorkManager manager;
    private String topic;
    private Class<?> dataType;
    private int qos;

    SampleSubscriber (MessageEndpointFactory ef, ActivationSpec spec, WorkManager manager) throws ResourceException {
        this.ef = ef;
        this.manager = manager;

        if (spec instanceof SampleActivationSpec) {
            SampleActivationSpec activationSpec = (SampleActivationSpec) spec;
            String clientId = MqttClient.generateClientId();
            this.topic = activationSpec.getTopic();
            try {
				client = new MqttClient("tcp://localhost:1883", clientId);

                MqttConnectOptions options = new MqttConnectOptions();
                options.setServerURIs(new String[] { "tcp://test.mosquitto.org:1883" });
    
                client.connect(options);
			} catch (MqttException e) {
                throw new ResourceException("Unable to create client.", e);
			}
        }
    }

    void subscribe() throws ResourceException {
        try {
            client.subscribe(topic, qos, this);
        } catch (MqttException ex) {
            throw new ResourceException("Unable to subscribe to topic.", ex);
        }
    }

    void close() throws ResourceException {
        try {
            client.disconnect();
            client.close();
        } catch (MqttException ex) {
            throw new ResourceException("Unable to subscribe to topic.", ex);
        }
    }

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message.getPayload());
        ObjectInputStream objectStream = new ObjectInputStream(inputStream);
        Serializable o = null;

        try {
            o = (Serializable) objectStream.readObject();
        } finally {
            objectStream.close();
            inputStream.close();
        }

		manager.scheduleWork(new SampleWork(topic, o, ef));
	}

}