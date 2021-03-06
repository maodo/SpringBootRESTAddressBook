package kh.springboot.addressbook;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import kh.mongo.MongoConnection;
import kh.springboot.addressbook.domain.Contact;

@Component
@Path("/addresses")
public class AddressBookResource {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AddressBookResource.class);
	
	/**
	 * Find and increment a sequence value for addressId from the sequences collection.
	 * @return
	 * @throws Exception
	 */
	public int getNextSequence() throws Exception {
		DB db = MongoConnection.getMongoDB();

		DBCollection sequences = db.getCollection("sequences");

		// fields to return
		DBObject fields = BasicDBObjectBuilder.start()
				.append("_id", 1)
				.append("value", 1).get();
		
		DBObject result = sequences.findAndModify(
				new BasicDBObject("_id", "addressId"), //query 
				fields, // what fields to return
				null, // no sorting
				false, //we don't remove selected document
				new BasicDBObject("$inc", new BasicDBObject("value", 1)), //increment value
				true, //true = return modified document
				true); //true = upsert, insert if no matching document

		return (int)result.get("value");
	}

	public void logEnvVars(){
		LOGGER.info("MONGODB_HOST_NAME: " + System.getenv("MONGODB_HOST_NAME"));
		LOGGER.info("MONGODB_DB_NAME: " + System.getenv("MONGODB_DB_NAME"));
		LOGGER.info("MONGODB_DB_PORT: " + System.getenv("MONGODB_DB_PORT"));
	}
	
	public String getServerIpAddress() throws UnknownHostException{
		InetAddress host = InetAddress.getLocalHost();
		String ip = host.getHostAddress();
		return ip;
	}
	
	//@GET
	//@Path("address")
	//@Produces(MediaType.APPLICATION_JSON)
	public Response getAddressByLastName(@QueryParam("lastname") String lastName) throws Exception {
		
		this.logEnvVars();
		
		DB db = MongoConnection.getMongoDB();
		
		DBObject query = new BasicDBObject("lastName", Pattern.compile(lastName));
		DBCursor c = db.getCollection("address").find(query);
		String jsonString = JSON.serialize(c);
		
		Response response = Response.status(Status.OK).entity(jsonString.toString()).build();
		return response;
	}	
	
	/**
	 * GET /addresses/{id}
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAddress(@PathParam("id") Long id) throws Exception {
		
		this.logEnvVars();
		DB db = MongoConnection.getMongoDB();
		
		DBObject query = new BasicDBObject("_id", id);
		DBCursor c = db.getCollection("address").find(query);
		String jsonString = JSON.serialize(c);
		
		Response response = Response.status(Status.OK).entity(jsonString.toString()).build();
		return response;
	}
	

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllAddresses(@QueryParam("lastname") String lastName) throws Exception {
		
		DB db = MongoConnection.getMongoDB();
		DBCursor c = null;
		
		DBObject query = null;
		if(lastName != null){
			query = new BasicDBObject("lastName", Pattern.compile(lastName));
			c = db.getCollection("address").find(query);
		}
		else{		
			c = db.getCollection("address").find();
		}
		
		String jsonString = JSON.serialize(c);

		JSONArray json = new JSONArray(jsonString);
		json.put(new JSONObject().put("server-ip", this.getServerIpAddress()));
		System.out.println(json.toString());
		Response response = Response.status(Status.OK).entity(json.toString()).build();
		return response;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insertAddress(Contact contactDomain) throws Exception {
		DBObject contact = BasicDBObjectBuilder.start()
				.append("_id", this.getNextSequence())
				.append("firstName", contactDomain.getFirstName())
				.append("lastName", contactDomain.getLastName()).get();
		DB db = MongoConnection.getMongoDB();
		db.getCollection("address").insert(contact);
		Response response = Response.status(Status.OK).build();
		return response;
	}
}

