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

import java.util.HashMap;
import java.util.Map;

// Import rabbitmq channel class
import com.rabbitmq.client.Channel;

// Import RabbitMq declarator
import io.latent.storm.rabbitmq.Declarator;

/**
 * Example Social Media Storm Declarator class (simple example without exchange and routing keys).
 * Will be passed to the main Social Media AMQP Spout and will declare specified RabbitMQ queue. Mainly needed in case if 
 * Storm spout would start to receive messages before they would be actually be sent
 *  
 * Documentation (no API, just an example of usage): https://github.com/ppat/storm-rabbitmq/blob/master/README.md (search for "Declarator") 
 */
public class ExampleSocialMediaStormDeclarator implements Declarator {
	private String strExchange; 
	private String strRoutingKey;
	private String strQueueName;
	private String strExchangeType;
	
	/**
	 *  Main constructor
	 *  
	 *  @param strExchange      rabbitmq exchange value 
	 *  @param strExchangeType  rabbitmq exhange type (e.g. topic, direct etc)
	 *  @param strRoutingKey    rabbitmq routing key
	 *  @param strQueueName     rabbitmq queue name
	 */
	public ExampleSocialMediaStormDeclarator( String strExchange, String strExchangeType, String strRoutingKey, String strQueueName ) {
		this.strExchange = strExchange;
		this.strRoutingKey = strRoutingKey;
		this.strQueueName = strQueueName;		
		this.strExchangeType = strExchangeType;
	}
	
	
	/**
	 * Main RabbitMQ declaration method. Will use RabbitMQ channel reference.
	 * 
	 * Rabbit MQ Channel API: https://www.rabbitmq.com/releases/rabbitmq-java-client/v3.1.5/rabbitmq-java-client-javadoc-3.1.5/ (search for "Channel")
	 * 
	 * @param channel  rabbitmq channel
	 */
	@Override
	public void execute( Channel channel ) {
		try {
			// Storm any possible arguments that could be passed 
			Map<String, Object> args = new HashMap<>();
			
			/* Declare the queue 
			 * 
			 * API: http://www.rabbitmq.com/releases/rabbitmq-java-client/v1.7.0/rabbitmq-java-client-javadoc-1.7.0/com/rabbitmq/client/Channel.html#queueDeclare(java.lang.String, boolean, boolean, boolean, boolean, java.util.Map))
		     * 
		     */
			channel.queueDeclare( this.strQueueName, true, false, false, args );
			
			/* Declare the exchange
			 * 
			 * API: http://www.rabbitmq.com/releases/rabbitmq-java-client/v1.7.0/rabbitmq-java-client-javadoc-1.7.0/com/rabbitmq/client/Channel.html#exchangeDeclare(java.lang.String, java.lang.String, boolean)
			 */
			channel.exchangeDeclare( this.strExchange, this.strExchangeType, true );
			
			/*
			 * Bind the queue
			 * 
			 * API: http://www.rabbitmq.com/releases/rabbitmq-java-client/v1.7.0/rabbitmq-java-client-javadoc-1.7.0/com/rabbitmq/client/Channel.html#queueBind(java.lang.String, java.lang.String, java.lang.String)
			 */
			channel.queueBind( this.strQueueName, this.strExchange, this.strRoutingKey );
                        			
		// Handle Exception and allow to continue
		} catch ( Exception e ) {
			System.err.println( "Failed to execute RabbitMQ declarations. Details: " + e.getMessage() );
			e.printStackTrace();
		}
	}
}

