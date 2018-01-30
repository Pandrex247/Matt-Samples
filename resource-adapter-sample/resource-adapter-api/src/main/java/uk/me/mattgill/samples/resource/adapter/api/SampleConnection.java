package uk.me.mattgill.samples.resource.adapter.api;

import java.io.Serializable;

import javax.resource.ResourceException;

public interface SampleConnection extends AutoCloseable {

    void publish(String topic, Serializable message, int qos) throws ResourceException;

    void publish(String topic, Serializable message) throws ResourceException;
}