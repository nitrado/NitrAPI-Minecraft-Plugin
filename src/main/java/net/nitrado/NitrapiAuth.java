package net.nitrado;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.http.HttpRequestInitializer;
import java.util.Arrays;

public class NitrapiAuth {

    /* Global instance of the HTTP transport. */
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    /* Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /*
    * Directory to store user credentials.
    * /ftproot/ is needed to see the file in your nitrado webinterface
    */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), MineNitrapi.instance.getCredentialsPath());

    /* Global instance of the FileDataStoreFactory */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    public static final String TOKEN_SERVER_URL = "https://oauth.nitrado.net/oauth/v2/token";
    public static final String AUTHORIZATION_SERVER_URL =
            "https://oauth.nitrado.net/oauth/v2/auth";

    /* Authorizes MineNitrapi to access user's protected data. */
    public static Credential authorize() throws Exception {

        // set up authorization code flow
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
                .authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_SERVER_URL),
                new ClientParametersAuthentication(
                        MineNitrapi.instance.getAppId(), MineNitrapi.instance.getAppSecret()),
                MineNitrapi.instance.getAppId(),
                AUTHORIZATION_SERVER_URL).setScopes(Arrays.asList(MineNitrapi.instance.getScopes()))
                .setDataStoreFactory(DATA_STORE_FACTORY).build();
        // authorize
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(
                MineNitrapi.instance.getIp()).setPort(MineNitrapi.instance.getPort()).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /* URL for Nitrado API. */
    public static class NitradoUrl extends GenericUrl {

        public NitradoUrl(String encodedUrl) {
            super(encodedUrl);
        }

        @Key
        public String fields;
    }

    public static HttpRequestFactory requestFactory() throws Exception{
        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        final Credential credential = authorize();
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request){
                        try {
                            credential.initialize(request);
                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        return requestFactory;
    }

    public static boolean ping() throws Exception {
        try {
            try {
                NitradoUrl url = new NitradoUrl("https://api.nitrado.net/ping");
                url.fields = "";
                HttpRequest request = requestFactory().buildGetRequest(url);
                request.execute();
                return true;
            } catch (HttpResponseException e) {
                System.out.println(e.getMessage());
                return false;
            }
        } catch (Throwable t) {
         t.printStackTrace();
        }
        System.out.println("Api ping was not successful! Please do /nitrapi auth");
        return false;
    }

}