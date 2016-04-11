/*
 * Copyright 2016 52°North.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.amqp;

import java.io.IOException;
import java.util.UUID;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Publisher {

    private static final Logger LOG = LoggerFactory.getLogger(Publisher.class);
    private final Connection connection;
    private final String id = UUID.randomUUID().toString();
    private Messenger messenger;

    protected Publisher(Connection c) {
        this.connection = c;
    }

    public void publish(CharSequence msg) {
        publish(msg, null);
    }

    public void publish(CharSequence msg, String subject) {
        LOG.debug("publishing message to target '{}'", connection.getRemoteURI());
        if (this.connection.isOpen()) {
            try {
                synchronized (this) {
                    if (this.messenger == null || this.messenger.stopped()) {
                        this.messenger = Messenger.Factory.create(id);
                        messenger.start();
                    }
                }

                Message message = Message.Factory.create();
                message.setAddress(this.connection.getRemoteURI().toString());

                if (subject != null) {
                    message.setSubject(subject);
                }

                message.setBody(new AmqpValue(msg));

                synchronized (this) {
                    messenger.put(message);
                    messenger.send();
                }
            } catch (IOException ex) {
                LOG.warn("Could not send message", ex);
            }
        }
        else {
            LOG.warn("Cannot send message. Connection already closed");
        }
    }

}