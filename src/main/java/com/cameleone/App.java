package com.cameleone;

import com.cameleone.jms.QueueSend;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import weblogic.jms.client.JMSConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Hello world!
 */
public class App {
    public static void main (String[] args) {
//        doJmsStuff();

        CamelContext camelContext = new DefaultCamelContext();
        ProducerTemplate producerTemplate = null;
        try {
            File outputDirectory = new File("./testDestination");
            File outputFile = new File("./testDestination/wasup.txt");
//            outputFile.delete();
//            outputDirectory.delete();
            camelContext.addRoutes(new RouteBuilder() {
                public void configure () {
                    from("file://testSource?noop=true").to("file://testDestination/");
                }
            });
            camelContext.addRoutes(new RouteBuilder() {
                public void configure () {
                    from("jms:queue:./test-module!testQueue").to("file://jmstest/result");
                }
            });
            System.out.println("Context starting     " + System.currentTimeMillis());
            while (camelContext.isStarting()) {
//                System.out.println("Context is starting");
            }
            ConnectionFactory connectionFactory = new JMSConnectionFactory();
            InitialContext ic = QueueSend.getInitialContext("t3://localhost:7001");

            ComponentsBuilderFactory.jms()
                    .connectionFactory((ConnectionFactory) ic.lookup("jms/TestConnectionFactory"))
                    .acknowledgementModeName("CLIENT_ACKNOWLEDGE")
                    .register(camelContext, "jms");
//                    .acknowledgementModeName()de(1)

            System.out.println("Context has started  " + System.currentTimeMillis());
            File output = new File("./testDestination/wasup.txt");
            System.out.println("Can read the output " + output.canRead());
            ProducerTemplate template = camelContext.createProducerTemplate();

            camelContext.start();
            long start = System.currentTimeMillis();
            while (!output.canRead()) {
//                System.out.println("To string of the file " + output.toString());
//                System.out.println("Waiting to read :)");
            }
            long end = System.currentTimeMillis();
            System.out.println("I have waited this many milliseconds: " + (end - start));
            System.out.println(Files.isRegularFile(output.toPath()));
            System.out.println(Files.size(output.toPath()));

            System.out.println("Can read the output " + output.canRead());
            System.out.println(camelContext.isStarted());
            System.out.println("Hello World!");
            System.out.println(camelContext.isStarted());
            /** JMS*/
            for (int i = 0; i < 10; i++) {
                System.out.println("Sending message");
                template.sendBody("jms:queue:./test-module!testQueue", "Test Message: " + i);
            }
            template.stop();
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            long beginStop = System.currentTimeMillis();
            camelContext.stop();
            while (camelContext.isStopping()) {

            }
            long endStop = System.currentTimeMillis();
            System.out.println("Stopped the context after this many millisecond: " + (endStop - beginStop));
        }
    }

    public static void doJmsStuff() {
//        if (args.length != 1) {
//            System.out.println("Usage: java examples.jms.queue.QueueSend WebLogicURL");
//            return;
//        }
        try {
//        InitialContext ic = QueueSend.getInitialContext("com.cameleone.jms.QueueSend");
        InitialContext ic = QueueSend.getInitialContext("t3://localhost:7001");
        QueueSend qs = new QueueSend();
            qs.init(ic, QueueSend.QUEUE);
        QueueSend.readAndSend(qs);
        qs.close();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
