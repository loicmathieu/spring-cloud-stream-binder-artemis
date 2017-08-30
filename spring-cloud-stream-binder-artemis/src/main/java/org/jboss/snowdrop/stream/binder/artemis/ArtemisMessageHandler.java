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

import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.messaging.Message;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import java.util.Objects;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ArtemisMessageHandler extends AbstractMessageHandler {

    private final String destination;

    private final ConnectionFactory connectionFactory;

    private final MessageConverter messageConverter;

    public ArtemisMessageHandler(String destination, ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        Objects.requireNonNull(message);
        // TODO handle partitions
        // TODO handle headers

        // TODO use JMSContext instead
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession()) {
            Topic topic = session.createTopic(destination);
            javax.jms.Message jmsMessage = messageConverter.toMessage(message.getPayload(), session);
            MessageProducer producer = session.createProducer(topic);
            producer.send(jmsMessage);
        }
    }

}
