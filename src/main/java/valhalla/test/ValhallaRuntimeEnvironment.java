import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import java.io.IOException;

import java.util.logging.Logger;

public class ValhallaRuntimeEnvironment {

    public enum HTTPStatusCode {
        SUCCESS(200),FAIL(400),INVALID_PATH(404),INVALID_MESSAGE(405),SERVER_PROBLEM(500),NOT_IMPLEMENTED(501);

        private int code;

        HTTPStatusCode(int code){
            this.code = code;
        }

        final public int getCode() {
            return this.code;
        }

        static public HTTPStatusCode getStatus(int code){
            HTTPStatusCode status = null;
            switch (code){
                case 200 -> status = HTTPStatusCode.SUCCESS;
                case 400 -> status = HTTPStatusCode.FAIL;
                case 404 -> status = HTTPStatusCode.INVALID_PATH;
                case 405 -> status = HTTPStatusCode.INVALID_MESSAGE;
                case 500 -> status = HTTPStatusCode.SERVER_PROBLEM;
                case 501 -> status = HTTPStatusCode.NOT_IMPLEMENTED;
            }
            return status;
        }
    }

    public enum ValhallaRequestType{
        ROUTE, OPTIMIZE, MATRIX, ISOCHRONE;

        static public String getURLSchema(ValhallaRequestType requestType) throws ValhallaException {
            String urlSchema;
            switch (requestType) {
                case ROUTE -> urlSchema = "/route?json=";
                case OPTIMIZE -> urlSchema = "/optimized_route?json=";
                case MATRIX -> urlSchema = "/sources_to_targets?json=";
                case ISOCHRONE -> urlSchema = "/isochrone?json=";
                default -> throw new ValhallaException("Request type does not exist: " + requestType +".");
            }
            return urlSchema;
        }
    }

    private boolean isOnline = true;

    private URI baseURI;

    private static Logger logger = Logger.getLogger(ValhallaRuntimeEnvironment.class.getName());

    public ValhallaRuntimeEnvironment() throws URISyntaxException {
        this.setBaseURI(new URI("http", null, "localhost", 8002, null, null, null));
    }

    public ValhallaRuntimeEnvironment(String baseURI) {
        try {
            this.setBaseURI(new URI(baseURI));
        } catch (URISyntaxException e) {
            isOnline = false;
        }
    }

    public boolean isOnline(){
        return isOnline;
    }

    public URI getBaseURI() {
        return this.baseURI;
    }
    public void setBaseURI(URI baseURI) {
        this.baseURI = baseURI;
    }

    private HttpRequest buildRequest(String URL,  String input) {
        return HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-type", "application/json")
                .POST(BodyPublishers.ofString(input))
                .build();
    }

    private String valhallaResponseHandler(HttpResponse<String> response) throws ValhallaException {

        // Get the response status code:
        int statusCode = response.statusCode();
        HTTPStatusCode status = HTTPStatusCode.getStatus(statusCode);

        // Check the response status code and act accordingly. Note that we don't expect a 201 code
        // as JSON I/O RPC is producing plain 200 with the result in the response body.
        if (status == HTTPStatusCode.SUCCESS) {
            logger.info("Request to Valhalla Server has been successful.");

            // Get the HTTP entity:
            String body = response.body();

            // Check the HTTP entity:
            if (body == null) {
                logger.severe("No content received from Valhalla Server.");
                throw new ValhallaException("No content received from Valhalla Server.");
            }

            return body;

        } else {

            switch (status) {
                case FAIL -> logger.severe("Request to Valhalla Server has failed.");
                case INVALID_PATH ->
                        logger.severe("Request to Valhalla Server has failed: invalid path.");
                case INVALID_MESSAGE ->
                        logger.severe("Request to Valhalla Server has failed: invalid message.");
                case SERVER_PROBLEM ->
                        logger.severe("Request to Valhalla Server has failed: server problem.");
                case NOT_IMPLEMENTED ->
                        logger.severe("Request to Valhalla Server has failed: not implemented.");
                default ->
                        {logger.severe("Unrecognized response from the Valhalla Server: " + statusCode);
                        throw new ValhallaException("Unrecognized response from the Valhalla Server: " + statusCode);}
            }

            String message;
            String body = response.body();
            if (body == null) {
                logger.severe("No content received from Valhalla Server.");
                message = "No content received from Valhalla Server.";
            } else {
                try {
                    message = body;
                } catch (RuntimeException e) {
                    logger.severe("Cannot read the output from Valhalla server response.");
                    throw new ValhallaException("Cannot read the output from Valhalla server response.", e);
                }
            }
            logger.severe("Bad request: " + message);
            throw new ValhallaException("Bad Request: " + message);
        }
    }

    public String valhallaSendRequest(String input, ValhallaRequestType requestType) throws ValhallaException{

        String url = this.getBaseURI().toString() + ValhallaRequestType.getURLSchema(requestType);
        HttpRequest request = buildRequest(url, input);

        logger.info("Sending synchronous request to Valhalla server (" + url + ").");

        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException ie) {
            throw new ValhallaException(ie);
        }

        return valhallaResponseHandler(response);
    }

}
