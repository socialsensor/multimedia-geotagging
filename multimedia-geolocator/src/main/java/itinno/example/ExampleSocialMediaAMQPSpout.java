/////////////////////////////////////////////////////////////////////////
//
// \xa9 University of Southampton IT Innovation, 2014
//
// Copyright in this software belongs to IT Innovation Centre of
// Gamma House, Enterprise Road, Southampton SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :	Vadim Krivcov
//	Created Date :	2014/02/09
//	Created for Project:	REVEAL
//
/////////////////////////////////////////////////////////////////////////
//
// Dependencies: None
//
/////////////////////////////////////////////////////////////////////////


package itinno.example;

// Import main RabbitMQ Spout super class
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Import rabbitmq declarator, message and actual AMQP spout
import io.latent.storm.rabbitmq.Declarator;
import io.latent.storm.rabbitmq.Message;
import io.latent.storm.rabbitmq.RabbitMQSpout;

// Import AMQP message Scheme and Spout Output Collector
import backtype.storm.spout.Scheme;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.tuple.Values;


/**
 * Subclass main third party AMQP Spout and inherit all its functionality (e.g. receive messages from RabbitMQ and pass them to the listening Bolts in real time, 
 * error corrections, RabbitMQ bus acknowledgements and much more)
 * 
 * Documentation (no API, just an example of usage): https://github.com/ppat/storm-rabbitmq/blob/master/README.md (search for "RabbitMQ Spout")
 */
public class ExampleSocialMediaAMQPSpout extends RabbitMQSpout {
	
	/**
	 * First example Social Media Client Framework constructor (no RabbitMQ Declarator) 
	 * 
	 * @param socialMediaScheme  helper class that will define JSON object parsing rules
	 */
	public ExampleSocialMediaAMQPSpout( Scheme socialMediaScheme ) {
		super( socialMediaScheme );
	}
	
	
	/**
	 * First example Social Media Client Framework constructor (with RabbitMQ Declarator) 
	 * 
	 * @param socialMediaScheme  helper class that will define JSON object parsing rules
	 * @param declarator         rabbitmq declarator helper class (mainly used to declare rabbitmq queue and connection parameters before any actions on that queue)
	 */
	public ExampleSocialMediaAMQPSpout( Scheme socialMediaScheme, Declarator declarator ) {
		super( socialMediaScheme, declarator );
	}
	
	
	/**
	 * Override main emit method from RabbitMQSpout in order to add additional functionality.
	 * Changes (e.g. additions to emited tuple):
	 *  - Add received message exchange
	 *  - Add received message routing
	 *  
	 * @param  tuple                                 storm emit tuple object
	 * @param  message                               actual message body that will be received from rabbitmq
	 * @param  spoutOutputCollector                  Storm spout output collector
	 * @return emit(tuple, getDeliveryTag(message))  callback to original emit metghod                         
	 */
	@Override
	protected List<Integer> emit( List<Object> tuple, Message message, SpoutOutputCollector spoutOutputCollector ) {
		// Create a map where message exchange/routing, as well as actual received message will be stored
		Map<Object, Object> tupleMap = new HashMap<Object, Object>();
		try {
			tupleMap = new HashMap<Object, Object>();
			tupleMap.put( "exchange", ( ( Message.DeliveredMessage ) message ).getExchange());
			tupleMap.put( "routing", ( ( Message.DeliveredMessage) message ).getRoutingKey());
			tupleMap.put( "message", tuple.get( 0 ) );
		
		// Handle Exception and allow to continue (as there will be many tuples)
		} catch ( Exception e ) {
			System.err.printf( "Exception occurred during Storm Social Media Spout tuple emit procedure. Details: %s.", e.getMessage() );
			e.printStackTrace();
		}
		// Pass the map as main tuple 
		return spoutOutputCollector.emit( new Values( tupleMap ), getDeliveryTag( message ) ) ;
	}
}	
