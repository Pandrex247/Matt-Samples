package uk.me.mattgill.samples.mqtt.test.message.out;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.resource.ConnectionFactoryDefinition;
import javax.resource.spi.TransactionSupport;

import uk.me.mattgill.samples.resource.adapter.api.SampleConnectionFactory;
import uk.me.mattgill.samples.resource.adapter.api.SampleConnection;

@ConnectionFactoryDefinition(name = "java:app/env/MyConnectionFactory",
        description = "My Connection Factory",
        interfaceName = "uk.me.mattgill.samples.resource.adapter.api.SampleConnectionFactory",
        resourceAdapter = "resource-adapter-rar-1.0-SNAPSHOT",
        minPoolSize = 1,
        maxPoolSize = 10,
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction
)
@Stateless
public class MqttMessageSender {

    @Resource(lookup = "java:app/env/MyConnectionFactory")
    private SampleConnectionFactory factory;

    private Logger logger = Logger.getLogger(MqttMessageSender.class.getName());

    @Schedule(hour = "*", minute = "*", second = "*/5", persistent = false)
    public void fireEvent() {
        
        String message = UUID.randomUUID().toString();
        
        try (SampleConnection conn = factory.getConnection()) {
            conn.publish("fish/payara/cloud/test/mosquitto", message, 1);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error sending message: {0}", ex.getMessage());
        }
    }

}
