package cs505embedded;

import cs505embedded.database.DBEngine;
import cs505embedded.httpfilters.AuthenticationFilter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sun.net.www.http.HttpClient;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;


public class Launcher {

    //sudo docker build -t cs505-embedded .
    //sudo docker run -d --rm -p 9000:9000 cs505-embedded

    //test
    //curl -H "X-Auth-API-Key: 12463865" http://localhost:9000/api/checkmydatabase


    public static DBEngine dbEngine;
    public static final String API_SERVICE_KEY = "12463865"; //Change this to your student id
    public static final int WEB_PORT = 9000;
    public static int test_mode = 0;
    public static int storage_mode = 0;
    public static boolean isWeb = false;

    public static void main(String[] args) throws Exception {

        //0=start nothing
        //1=start DB alone
        //2=start DB with local query load
        //3=start web server and database server
        //4=start web server and database server, load on web interface


        if(args.length == 0) {
            System.out.println("Usage: java -jar cs505-embedded-demo-1.0-SNAPSHOT.jar [test mode] [storage mode] [insert count]");
            System.out.println(" ");
            System.out.println("[test mode]");
            System.out.println("0=start nothing");
            System.out.println("2=start DB with local query load");
            System.out.println("3=start web server and database server");
            System.out.println("4=start web server and database server, load on web interface");
            System.out.println(" ");
            System.out.println("[storage mode]");
            System.out.println("0=in-memory");
            System.out.println("1=on-disk");
            System.out.println("[number of records]");
            System.exit(0);
        } else {
            test_mode = Integer.valueOf(args[0]);
            if(args.length > 1) {
                storage_mode = Integer.valueOf(args[1]);
            }

            System.out.println("Test Mode: " + test_mode);
            System.out.println("Storage Mode: " + storage_mode);

            if(test_mode > 0) {
                System.out.println("Starting Database...");
                //Embedded database initialization
                dbEngine = new DBEngine();
                System.out.println("Database Started...");

            }

            if(test_mode > 2) {
                //Embedded HTTP initialization
                startServer();
                isWeb = true;
            }

            if(test_mode == 2) {
                int record_count = Integer.valueOf(args[2]);
                System.out.println("Inserting " + record_count + " records into DB");
                long start_insert = System.currentTimeMillis();
                String remoteIP = "127.0.0.1";
                for(int i = 0; i<record_count; i++) {
                    long access_ts = System.currentTimeMillis();
                    //System.out.println("IP: " + remoteIP + " Timestamp: " + access_ts);

                    //insert access data
                    String insertQuery = "INSERT INTO accesslog VALUES ('" + remoteIP + "'," + access_ts + ")";
                    Launcher.dbEngine.executeUpdate(insertQuery);
                }
                long runtime = System.currentTimeMillis() - start_insert;
                System.out.println("Insert time: " + runtime + "ms");
                System.out.println("Query AVG TS");
                long start_q = System.currentTimeMillis();
                System.out.println("avg_ts: " + Launcher.dbEngine.getAvgts());
                long runtime_q = System.currentTimeMillis() - start_q;
                System.out.println("Query time: " + runtime_q + "ms");
            }

            if(test_mode == 4) {

                int record_count = Integer.valueOf(args[2]);
                System.out.println("Inserting " + record_count + " records into DB over web");

                long start_insert = System.currentTimeMillis();
                for(int i = 0; i<record_count; i++) {
                    bumpAccessLog();
                }
                long runtime = System.currentTimeMillis() - start_insert;
                System.out.println("Insert time: " + runtime + "ms");
                System.out.println("Query AVG TS");
                long start_q = System.currentTimeMillis();
                System.out.println("avg_ts: " + Launcher.dbEngine.getAvgts());
                long runtime_q = System.currentTimeMillis() - start_q;
                System.out.println("Query time: " + runtime_q + "ms");

            }

            if(!isWeb) {
                try {
                    while (true) {
                        Thread.sleep(5000);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }





        }

    }

    public static void bumpAccessLog() throws Exception
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try
        {
            //Define a HttpGet request; You can choose between HttpPost, HttpDelete or HttpPut also.
            //Choice depends on type of method you will be invoking.
            HttpGet getRequest = new HttpGet("http://localhost:9000/api/bumpaccesslog");

            //Set the API media type in http accept header
            getRequest.addHeader("accept", "application/json");
            getRequest.addHeader("X-Auth-API-Key","1234");


                //Send the request; It will immediately return the response in HttpResponse object
                HttpResponse response = httpClient.execute(getRequest);

                //verify the valid error code first
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200)
                {
                    throw new RuntimeException("Failed with HTTP error code : " + statusCode);
                }

                //Now pull back the response object
                HttpEntity httpEntity = response.getEntity();
                String apiOutput = EntityUtils.toString(httpEntity);

                //Lets see what we got from API
                //System.out.println(apiOutput); //<user id="10"><firstName>demo</firstName><lastName>user</lastName></user>


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally
        {
            //Important: Close the connect
            httpClient.getConnectionManager().shutdown();
        }
    }

    private static void startServer() throws IOException {

        final ResourceConfig rc = new ResourceConfig()
                .packages("cs505embedded.httpcontrollers")
                .register(AuthenticationFilter.class);

        System.out.println("Starting Web Server...");
        URI BASE_URI = UriBuilder.fromUri("http://0.0.0.0/").port(WEB_PORT).build();
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);

        try {
            httpServer.start();
            System.out.println("Web Server Started...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
