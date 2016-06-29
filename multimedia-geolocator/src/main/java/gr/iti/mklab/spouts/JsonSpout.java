package gr.iti.mklab.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.CompletableSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Map;

import static backtype.storm.utils.Utils.tuple;


/**
 * A simple json spout, which emits json tuples found in the resources folder of the project
 *
 * @author kandreadou
 */
public class JsonSpout extends BaseRichSpout implements CompletableSpout {

    private static Logger _logger = LoggerFactory.getLogger(JsonSpout.class);
    private SpoutOutputCollector collector;
    private ListIterator<File> it;


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("prepared_itinno_json"));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.collector = spoutOutputCollector;
        // in case of test topology, provide the full path in the File argument
        it = Arrays.asList(new File("/home/georgekordopatis/git/reveal_restlet/topology_src/certhTopologies/multimedia-geolocator/samples").listFiles()).listIterator();
    }

    @Override
    public void nextTuple() {
        if (it.hasNext())
            collector.emit(tuple(getFileContentAsString(it.next())));
        else
            this.close();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public Object startup() {
        return null;
    }

    @Override
    public Object cleanup() {
        return null;
    }

    @Override
    public Object exhausted_QMARK_() {
        return null;
    }

    private String getFileContentAsString(File f) {
        try {
            return IOUtils.toString(new FileInputStream(f));
        } catch (IOException ioe) {
            _logger.error("Could not read file " + f, ioe);
            return null;
        }
    }
}
