package de.otto.rx.composer.client;

import org.junit.Test;

import static de.otto.rx.composer.client.ClientConfig.noResiliency;
import static de.otto.rx.composer.client.ClientConfig.singleRetry;
import static de.otto.rx.composer.client.DefaultRef.noRetries;
import static de.otto.rx.composer.client.DefaultRef.singleRetry;
import static de.otto.rx.composer.client.ServiceClients.defaultClients;
import static de.otto.rx.composer.client.ServiceClients.defaultClientsWith;
import static de.otto.rx.composer.client.ServiceClients.serviceClients;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ServiceClientsTest {

    @Test
    public void shouldConfigureDefaultServiceClients() {
        ServiceClients serviceClients = defaultClients();

        assertThat(serviceClients.getDefault().getClientConfig().getRef(), is(DefaultRef.singleRetry));
        assertThat(serviceClients.getBy(singleRetry).getClientConfig().getRef(), is(DefaultRef.singleRetry));
        assertThat(serviceClients.getBy(noRetries).getClientConfig().getRef(), is(DefaultRef.noRetries));
        assertThat(serviceClients.getBy(DefaultRef.noResiliency).getClientConfig().getRef(), is(DefaultRef.noResiliency));
    }

    @Test
    public void shouldConfigureDefaultServiceClientsWithOtherDefault() {
        ServiceClients serviceClients = defaultClientsWith(noResiliency());

        assertThat(serviceClients.getDefault().getClientConfig().getRef(), is(DefaultRef.noResiliency));
        assertThat(serviceClients.getBy(singleRetry).getClientConfig().getRef(), is(DefaultRef.singleRetry));
        assertThat(serviceClients.getBy(noRetries).getClientConfig().getRef(), is(DefaultRef.noRetries));
        assertThat(serviceClients.getBy(DefaultRef.noResiliency).getClientConfig().getRef(), is(DefaultRef.noResiliency));
    }

    @Test
    public void shouldConfigureDefaultServiceClientsWithAdditionalDefault() {
        ServiceClients serviceClients = defaultClientsWith(noResiliency(()->"test", 100, 50));

        assertThat(serviceClients.getDefault().getClientConfig().getRef().name(), is("test"));
        assertThat(serviceClients.getBy(singleRetry).getClientConfig().getRef(), is(DefaultRef.singleRetry));
        assertThat(serviceClients.getBy(noRetries).getClientConfig().getRef(), is(DefaultRef.noRetries));
        assertThat(serviceClients.getBy(DefaultRef.noResiliency).getClientConfig().getRef(), is(DefaultRef.noResiliency));
        assertThat(serviceClients.getBy(()->"test").getClientConfig().getRef().name(), is("test"));
        assertThat(serviceClients.getBy(()->"test").getClientConfig().getRetries(), is(0));
        assertThat(serviceClients.getBy(()->"test").getClientConfig().isResilient(), is(false));
        assertThat(serviceClients.getBy(()->"test").getClientConfig().getConnectTimeout(), is(100));
        assertThat(serviceClients.getBy(()->"test").getClientConfig().getReadTimeout(), is(50));
    }

    @Test
    public void shouldConfigureSingleServiceClient() {
        ServiceClients serviceClients = serviceClients(noResiliency());

        ServiceClient serviceClient = serviceClients.getDefault();
        assertThat(serviceClient.getClientConfig().isResilient(), is(false));
    }

    @Test
    public void shouldConfigureMoreServiceClients() {
        ServiceClients serviceClients = serviceClients(noResiliency(), singleRetry());

        ServiceClient serviceClient = serviceClients.getBy(singleRetry);
        assertThat(serviceClient.getClientConfig().isResilient(), is(true));
        assertThat(serviceClient.getClientConfig().getRetries(), is(1));
    }

}
