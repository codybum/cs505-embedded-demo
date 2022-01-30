package cs505embedded.httpcontrollers;

import com.google.gson.Gson;
import cs505embedded.Launcher;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api")
public class API {

    @Inject
    private javax.inject.Provider<org.glassfish.grizzly.http.server.Request> request;

    private Gson gson;

    public API() {
        gson = new Gson();
    }

    //check local
    //curl --header "X-Auth-API-key:1234" "http://localhost:8081/api/checkmydatabase"

    //check remote
    //curl --header "X-Auth-API-key:1234" "http://[linkblueid].cs.uky.edu:8081/api/checkmydatabase"

    @GET
    @Path("/checkmydatabase")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkMyEndpoint() {
        String responseString = "{}";
        try {

            //get remote ip address from request
            String remoteIP = request.get().getRemoteAddr();
            //get the timestamp of the request
            long access_ts = System.currentTimeMillis();
            System.out.println("IP: " + remoteIP + " Timestamp: " + access_ts);

            Map<String,String> responseMap = new HashMap<>();
            if(Launcher.dbEngine.databaseExist("mydatabase")) {

                if(Launcher.dbEngine.tableExist("accesslog")) {
                    responseMap.put("success", Boolean.TRUE.toString());
                    responseMap.put("status_desc","accesslog table exists");
                } else {
                    responseMap.put("success", Boolean.FALSE.toString());
                    responseMap.put("status_desc","access_log table does not exist!");
                }

            } else {
                responseMap.put("success", Boolean.FALSE.toString());
                responseMap.put("status_desc","database does not exist!");
            }

            responseString = gson.toJson(responseMap);


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/bumpaccesslog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bumpAccessLog() {
        String responseString = "{}";
        try {

            //get remote ip address from request
            String remoteIP = request.get().getRemoteAddr();
            //get the timestamp of the request
            long access_ts = System.currentTimeMillis();
            //System.out.println("IP: " + remoteIP + " Timestamp: " + access_ts);

            //insert access data
            String insertQuery = "INSERT INTO accesslog VALUES ('" + remoteIP + "'," + access_ts + ")";
            Launcher.dbEngine.executeUpdate(insertQuery);
            Map<String,String> status = new HashMap<>();
            status.put("status","1");
            responseString = gson.toJson(status);


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        //return accesslog data
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getaccesslog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccessLog() {
        String responseString = "{}";
        try {

            //get remote ip address from request
            String remoteIP = request.get().getRemoteAddr();
            //get the timestamp of the request
            long access_ts = System.currentTimeMillis();
            System.out.println("IP: " + remoteIP + " Timestamp: " + access_ts);

            //insert access data
            String insertQuery = "INSERT INTO accesslog VALUES ('" + remoteIP + "'," + access_ts + ")";
            Launcher.dbEngine.executeUpdate(insertQuery);

            //get accesslog data
            List<Map<String,String>> accessMapList = Launcher.dbEngine.getAccessLogs();
            responseString = gson.toJson(accessMapList);


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        //return accesslog data
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

}
