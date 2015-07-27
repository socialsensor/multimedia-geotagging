package gr.iti.mklab.topology;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import gr.iti.mklab.spouts.JsonSpout;
import gr.iti.mklab.topology.bolts.TestGeolocatorBolt;

/**
 * Test topology for debugging
 * @author gkordo
 *
 */
public class TestTopologyRunner {

	private static String resPath = "";
	
	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
		
		Config conf = new Config();
		conf.put(Config.NIMBUS_HOST, "localhost");
		conf.put(Config.TOPOLOGY_DEBUG, true);
		conf.setDebug(true);
		
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("JsonSpout", new JsonSpout());
        builder.setBolt("TestBolt", new TestGeolocatorBolt("TestBolt","http://localhost:8182",resPath)).shuffleGrouping("JsonSpout");
        
        StormSubmitter.submitTopology("multimedia-geolocator", conf, builder.createTopology());
    }
}