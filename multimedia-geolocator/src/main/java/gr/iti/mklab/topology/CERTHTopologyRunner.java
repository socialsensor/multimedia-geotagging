package gr.iti.mklab.topology;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.SpoutDeclarer;
import backtype.storm.topology.TopologyBuilder;
import gr.iti.mklab.topology.bolts.MultimediaGeolocatorBolt;

import com.rabbitmq.client.ConnectionFactory;
import io.latent.storm.rabbitmq.Declarator;
import io.latent.storm.rabbitmq.config.ConnectionConfig;
import io.latent.storm.rabbitmq.config.ConsumerConfig;
import io.latent.storm.rabbitmq.config.ConsumerConfigBuilder;
import itinno.example.ExampleSocialMediaAMQPSpout;
import itinno.example.ExampleSocialMediaStormDeclarator;
import uniko.west.util.JacksonScheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CERTHTopologyRunner {
    
    public static String topologyName = "multimedia-geolocator";  // the name is not important at this place it is just used in the storm UI

    public static void main(String[] args) {
		TopologyBuilder builder;

		// Storm Spouts
		IRichSpout stormExampleSocialMediaAMQPSpout;
		SpoutDeclarer spoutDeclarer;

		// Storm RabbitMQ queue declarator
		Declarator declarator;
		String spoutId = "rabbitmqSpout";

		// Main Storm Social Media Properties file
		File configFile = new File(args[0]);
		File pServerConfigFile = new File(args[1]);
		// URL of restlet service
		String restletURL = args[2];
		String rmqExchange = args[3];
		String nimbusHost = args[4];

		// Create Java properties file from the passed configuration file
		Properties properties = new Properties();
		Properties pServerConfig = new Properties();
		try {
			properties.load(new FileInputStream(configFile));
			pServerConfig.load(new FileInputStream(pServerConfigFile));
		} catch (IOException ex) {
			Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.SEVERE,
					null, ex);
		}

		// Get all the needed RabbitMQ connection properties from the
		// configuration file
		String rmqHost = properties.getProperty("rmqhost", "localhost");
		int rmqPort = Integer.parseInt(properties
				.getProperty("rmqport", "5672"));
		String rmqUsername = properties.getProperty("rmqusername", "guest");
		String rmqPassword = properties.getProperty("rmqpassword");
		int rmqHeartBeat = Integer.parseInt(properties.getProperty(
				"rmqheartbeat", "10"));
		String rmqQueueName = properties.getProperty("rmqqueuename", "test");
		String rmqExchangeType = properties.getProperty("rmqexchangetype",
				"topic");
		String rmqRouting = properties
				.getProperty("rmqrouting", "test-routing");

		String emitFieldsId = properties
				.getProperty("emit_fields_id", "object");

		// Get Storm Topology configuration parameters
		boolean topologyDebug = Boolean.valueOf(properties.getProperty(
				"topology_debug", "false"));

		// Get Storm Spout configuration parameters
		boolean spoutDebug = Boolean.valueOf(properties.getProperty(
				"spout_debug", "false"));
		int rmqPrefetch = Integer.parseInt(properties.getProperty(
				"spout_rmqprefetch", "200"));
		int maxSpoutPending = Integer.parseInt(properties.getProperty(
				"spout_max_spout_pending", "200"));

		JacksonScheme jsonScheme = new JacksonScheme();

		/*
		 * Create RabbitMQ connection configuration Documentation (no API, just
		 * an example of usage):
		 * https://github.com/ppat/storm-rabbitmq/blob/master/README.md (search
		 * for "RabbitMQ Spout")
		 */
		ConnectionConfig connectionConfig = new ConnectionConfig(rmqHost,
				rmqPort, rmqUsername, rmqPassword,
				ConnectionFactory.DEFAULT_VHOST, rmqHeartBeat);
		Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.INFO,
				"Initialised RabbitMQ connection configuration object.");
		/*
		 * Create Storm Spout configuration builder Documentation (no API, just
		 * an example of usage):
		 * https://github.com/ppat/storm-rabbitmq/blob/master/README.md (search
		 * for "RabbitMQ Spout")
		 */
		// Customer configuration builder
		ConsumerConfigBuilder spoutConfigBuilder = new ConsumerConfigBuilder();
		spoutConfigBuilder.connection(connectionConfig);
		spoutConfigBuilder.queue(rmqQueueName);
		spoutConfigBuilder.prefetch(rmqPrefetch);
		spoutConfigBuilder.requeueOnFail();
		Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.INFO,
				"Initialised Spout configuration builder.");

		/*
		 * Build Storm spout configuration Documentation (no API, just an
		 * example of usage):
		 * https://github.com/ppat/storm-rabbitmq/blob/master/README.md (search
		 * for "RabbitMQ Spout")
		 */
		ConsumerConfig spoutConfig = spoutConfigBuilder.build();
		Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.INFO,
				"Initialised Spout configuration builder.");

		/*
		 * Create a AMQP Declarator (will declare queue if it does not exist on
		 * the time of the Storm launch) Documentation (no API, just an example
		 * of usage):
		 * https://github.com/ppat/storm-rabbitmq/blob/master/README.md (search
		 * for "Declarator")
		 */
		declarator = new ExampleSocialMediaStormDeclarator(rmqExchange,
				rmqExchangeType, rmqRouting, rmqQueueName);

		/*
		 * Initialise Social Media Spout API:
		 * http://nathanmarz.github.io/storm/doc-0.8.1/index.html (search for
		 * "IRichSpout")
		 */
		stormExampleSocialMediaAMQPSpout = new ExampleSocialMediaAMQPSpout(
				jsonScheme, declarator);
		Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.INFO,
				"Initialised AMQP Spout object on exchange " + rmqExchange);

		/*
		 * Create a simple STORM topology configuration file Documentation (no
		 * API, just an example of usage):
		 * https://github.com/ppat/storm-rabbitmq/blob/master/README.md (search
		 * for "Config")
		 */
		Config conf = new Config();
		conf.put(Config.NIMBUS_HOST, nimbusHost);
		conf.put(Config.TOPOLOGY_DEBUG, topologyDebug);
		conf.setDebug(topologyDebug);
		Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.INFO,
				"Initialised main example Storm confuration.");

		/*
		 * Initialise Storm Topology API:
		 * http://nathanmarz.github.io/storm/doc-0.8.1/index.html (search for
		 * "TopologyBuilder")
		 */
		builder = new TopologyBuilder();

		/*
		 * Define a new Spout in the topology API:
		 * http://nathanmarz.github.io/storm/doc-0.8.1/index.html (search for
		 * "SpoutDeclarer")
		 */
		spoutDeclarer = builder.setSpout(spoutId,
				stormExampleSocialMediaAMQPSpout);
		Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.INFO,
				"Declared AMQP Spout to the example Storm topology.");

		// Add configuration to the StoputDeclarer
		spoutDeclarer.addConfigurations(spoutConfig.asMap());

		/*
		 * Explanation taken from: https://github.com/ppat/storm-rabbitmq Set
		 * MaxSpoutPending value to the same value as RabbitMQ pre-fetch count
		 * (set initially in in the ConsumerConfig above). It is possible to
		 * tune them later separately, but MaxSpoutPending should always be <=
		 * Prefetch
		 */
		spoutDeclarer.setMaxSpoutPending(maxSpoutPending);
		spoutDeclarer.setDebug(spoutDebug);

		BoltDeclarer boltDeclarer;

		MultimediaGeolocatorBolt multimediaGeolocator = new MultimediaGeolocatorBolt(emitFieldsId, restletURL);
        boltDeclarer = builder.setBolt("multimediaGeolocator", multimediaGeolocator);
        boltDeclarer.shuffleGrouping(spoutId);
        
        try {
            // Submit the topology to the distribution cluster that will be defined in Storm client configuration file or via cmd as a parameter ( e.g. nimbus.host=localhost )
            StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
        } catch (AlreadyAliveException | InvalidTopologyException ex) {
            Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(CERTHTopologyRunner.class.getName()).log(Level.INFO, "Submitted topology : " + topologyName);

    }

}
