package uniko.west.util;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 * @author nico
 * 
 * Deserialisation scheme for JSON values using the jackson library.
 */
public class JacksonScheme implements Scheme {
    private static final long serialVersionUID = -7734176307841199017L;
    private final ObjectMapper mapper;
    
    public JacksonScheme() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<Object> deserialize(byte[] bytes) {
        Object json = null;
        try {
            json = mapper.readValue(bytes, Object.class);
        } catch (IOException ex) {
            Logger.getLogger(JacksonScheme.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.singletonList(json);
    }


    @Override
    public Fields getOutputFields() {
        return new Fields("object");
    }
}
