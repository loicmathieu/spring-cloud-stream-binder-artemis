/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.snowdrop.stream.binder.artemis;

import org.jboss.snowdrop.stream.binder.artemis.provisioning.ArtemisProvisioningProvider;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.dsl.jms.JmsMessageDrivenChannelAdapter;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.messaging.MessageHandler;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Topic;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ArtemisMessageChannelBinder
        extends AbstractMessageChannelBinder<ConsumerProperties, ProducerProperties, ArtemisProvisioningProvider> {

    private static final String[] DEFAULT_HEADERS = new String[0];

    private final ConnectionFactory connectionFactory;

    private final MessageConverter messageConverter;

    public ArtemisMessageChannelBinder(ArtemisProvisioningProvider provisioningProvider,
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        super(true, DEFAULT_HEADERS, provisioningProvider);
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
            ProducerProperties properties) throws Exception {

        System.out.printf("Creating producer handler: %s\n", destination.getName());

        if (properties.isPartitioned()) {
            // TODO
            throw new UnsupportedOperationException();
        } else {
            return new ArtemisMessageHandler(destination.getName(), connectionFactory, messageConverter);
        }
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group,
            ConsumerProperties properties) throws Exception {
        try (JMSContext context = connectionFactory.createContext()) {
            Topic topic = context.createTopic(destination.getName());

            AbstractMessageListenerContainer messageListenerContainer = getMessageListenerContainer(topic, group);
            return new JmsMessageDrivenChannelAdapter(messageListenerContainer,
                    new ChannelPublishingJmsMessageListener());
        }
    }

    // TODO extract
    private AbstractMessageListenerContainer getMessageListenerContainer(Topic topic, String group) {
        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
        listenerContainer.setDestination(topic);
        listenerContainer.setSubscriptionName(group);
        listenerContainer.setPubSubDomain(true);
        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.setSessionTransacted(true);
        listenerContainer.setSubscriptionDurable(true);
        listenerContainer.setSubscriptionShared(true);
        return listenerContainer;
    }
}