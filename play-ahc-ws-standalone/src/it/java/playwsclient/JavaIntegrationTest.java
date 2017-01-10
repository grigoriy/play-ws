/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package playwsclient;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.StandaloneWSClient;
import play.libs.ws.StandaloneWSResponse;
import play.libs.ws.ahc.StandaloneAhcWSClient;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JavaIntegrationTest {

    private ActorSystem system;
    private ActorMaterializer materializer;
    private DefaultAsyncHttpClient ahcClient;
    private StandaloneWSClient client;

    @Before
    public void before() {
        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                .setMaxRequestRetry(0)
                .setShutdownQuietPeriod(0)
                .setShutdownTimeout(0).build();

        String name = "wsclient";
        system = ActorSystem.create(name);
        ActorMaterializerSettings settings = ActorMaterializerSettings.create(system);

        materializer = ActorMaterializer.create(settings, system, name);
        ahcClient = new DefaultAsyncHttpClient(config);
    }

    @After
    public void after() {
        try {
            client.close();
            system.terminate();
            ahcClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClient() {
        client = new StandaloneAhcWSClient(ahcClient, materializer);
        CompletionStage<StandaloneWSResponse> completionStage = client.url("http://www.google.com").get();

        try {
            final StandaloneWSResponse response = completionStage.toCompletableFuture().get();
            int status = response.getStatus();
            assertEquals(status, 200);
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}