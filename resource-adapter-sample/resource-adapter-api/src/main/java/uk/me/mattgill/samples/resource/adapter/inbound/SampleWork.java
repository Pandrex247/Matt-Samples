package uk.me.mattgill.samples.resource.adapter.inbound;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;

public class SampleWork implements Work {

    private Logger logger = Logger.getLogger(SampleWork.class.getName());

    private final String topic;
    private final Serializable message;
    private final MessageEndpointFactory factory;
    private MessageEndpoint endpoint;

    SampleWork (String topic, Serializable message, MessageEndpointFactory factory) {
        this.topic = topic;
        this.message = message;
        this.factory = factory;
    }

	@Override
	public void run() {
        Method methods[] = factory.getEndpointClass().getMethods();

        Method method = null;

        for(Method test : methods) {
            if (test.getName().equals("onMessage") && test.getParameterCount() == 2
                    && test.getParameterTypes()[0].equals(String.class)
                    && test.getParameterTypes()[1].isAssignableFrom(Serializable.class)) {
                method = test;
                break;
            }
        }
        if (method != null) {
            try {
                endpoint = factory.createEndpoint(null);
                endpoint.beforeDelivery(method);
                if (message != null) {
                    method.invoke(endpoint, topic, message);
                }
                endpoint.afterDelivery();
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ResourceException ex) {
                logger.log(Level.SEVERE, null, ex);
            } finally {
                if (endpoint != null) {
                    endpoint.release();
                }
            }
        }
	}

	@Override
	public void release() {
		if (endpoint != null) {
            endpoint.release();
        }
	}

}