package cs505embedded.httpfilters;


import cs505embedded.Launcher;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;


@Provider
public class AuthenticationFilter implements ContainerRequestFilter {


    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {

            // Then check is the service key exists and is valid.
            String serviceKey = requestContext.getHeaderString("X-Auth-API-Key");
            if (serviceKey != null) {
                if(Launcher.API_SERVICE_KEY != "0") {
                    return;
                } else {
                    System.out.println("CHANGE YOUR Launcher.API_SERVICE_KEY!");
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("[" + serviceKey + "] Must not be 0!\n").build());
                }
            } else {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Header: X-Auth-API-Key NOT FOUND!\n").build());
            }

        } catch (Exception e) {
            requestContext.abortWith(Response.serverError().build());
            e.printStackTrace();
        }
    }
}
