package gr.iti.mklab.test;

import backtype.storm.Config;
import backtype.storm.ILocalCluster;
import backtype.storm.Testing;
import backtype.storm.generated.StormTopology;
import backtype.storm.testing.*;
import backtype.storm.topology.TopologyBuilder;
import gr.iti.mklab.spouts.JsonSpout;
import gr.iti.mklab.topology.bolts.TestGeolocatorBolt;
import junit.framework.TestCase;

import java.util.Map;

/**
 * A test case of the module
 */
public class GeolocatorTest extends TestCase {

	private String resPath;
	private String restletURL;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.restletURL = "http://localhost:8182";

		// provide a full path where the result of the topology will be stored
		this.resPath = "/home/georgekordopatis/storm_test_logs";
	}

	//The test will fail because of a timeout. It takes longer than the hard-coded timeout of 5000ms
	public void testGeolocatorTopology() throws Exception {

		MkClusterParam mkClusterParam = new MkClusterParam();
		mkClusterParam.setSupervisors(4);
		Config daemonConf = new Config();
		daemonConf.put(Config.STORM_LOCAL_MODE_ZMQ, false);
		mkClusterParam.setDaemonConf(daemonConf);

		Testing.withLocalCluster(mkClusterParam, (ILocalCluster cluster) -> {

			StormTopology topology = getGeolocatorBuilder().createTopology();

			Config conf = new Config();
			conf.setNumWorkers(4);
			CompleteTopologyParam completeTopologyParam = new CompleteTopologyParam();
			completeTopologyParam.setStormConf(conf);
			completeTopologyParam.setMockedSources(new MockedSources());

			Map result = Testing.completeTopology(cluster, topology,
					completeTopologyParam);
		});
	}

	//Build the topology
	private TopologyBuilder getGeolocatorBuilder(){
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("JsonSpout", new JsonSpout());
		builder.setBolt("TestBolt", new TestGeolocatorBolt("TestBolt", restletURL, resPath)).shuffleGrouping("JsonSpout");
		return builder;
	}
}