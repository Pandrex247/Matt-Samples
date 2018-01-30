package uk.me.mattgill.samples.mqtt.test.message.in;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import uk.me.mattgill.samples.resource.adapter.api.SampleListener;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "topic", propertyValue = "fish/payara/cloud/test/mosquitto")
})
public class MqttMessageDrivenBean implements SampleListener {
     
    private Logger logger = Logger.getLogger(MqttMessageDrivenBean.class.getName());
    
    public void onMessage(String topic, Serializable message) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("Topic", topic);
        builder.add("Message", message.toString());
        logger.info(builder.build().toString());
    }
}
