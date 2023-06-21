public class Valhalla {

    private boolean isOnline = false;
    ValhallaRuntimeEnvironment valhalla;
    ValhallaOutputDeserializer deserializer;

    public Valhalla(){
        String service = "http://localhost:8002";
        valhalla = new ValhallaRuntimeEnvironment(service);
        isOnline = valhalla.isOnline();
        deserializer = new ValhallaOutputDeserializer();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String route(String input) throws ValhallaException {
        return valhalla.valhallaSendRequest(input, ValhallaRuntimeEnvironment.ValhallaRequestType.ROUTE);
    }

    public String optimized_route(String input) throws ValhallaException {
        return valhalla.valhallaSendRequest(input, ValhallaRuntimeEnvironment.ValhallaRequestType.OPTIMIZE);
    }

    public ValhallaOutputDeserializer.Matrix matrix(String input) throws ValhallaException {
        String response = valhalla.valhallaSendRequest(input, ValhallaRuntimeEnvironment.ValhallaRequestType.MATRIX);
        deserializer.setJson(response);
        return deserializer.deserializeMatrixOutput();
    }

    public String isochrone(String input) throws ValhallaException {
        return valhalla.valhallaSendRequest(input, ValhallaRuntimeEnvironment.ValhallaRequestType.ISOCHRONE);
    }




}
