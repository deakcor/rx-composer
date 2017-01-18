package de.otto.rx.composer.providers;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.http.HttpClient;
import org.glassfish.jersey.message.internal.Statuses;
import org.junit.Test;
import rx.observables.BlockingObservable;

import javax.ws.rs.core.Response;
import java.net.ConnectException;
import java.util.Iterator;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
import static de.otto.rx.composer.providers.ContentProviders.resilientContentFrom;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.fromCallable;
import static rx.Observable.just;

public class ResilientHttpGetContentProviderTest {

    @Test
    public void shouldFetchContentByUrl() throws Exception {
        // given
        final Response response = someResponse(200, "Foo");
        final HttpClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = resilientContentFrom(mockClient, "/test", TEXT_PLAIN, commandKey());
        final Content content = contentProvider.getContent(X, emptyParameters()).toBlocking().single();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldFetchContentByUriTemplate() {
        // given
        final Response response = someResponse(200, "FooBar");
        final HttpClient mockClient = someHttpClient(response, "/test?foo=bar");
        // when
        final ContentProvider contentProvider = resilientContentFrom(mockClient, fromTemplate("/test{?foo}"), TEXT_PLAIN, commandKey());
        final Content content = contentProvider.getContent(X, parameters(of("foo", "bar"))).toBlocking().single();
        // then
        verify(mockClient).get("/test?foo=bar", TEXT_PLAIN_TYPE);
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("FooBar"));
    }

    @Test
    public void shouldIgnoreEmptyContent() {
        // given
        final Response response = someResponse(200, "");
        final HttpClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = resilientContentFrom(mockClient, "/test", TEXT_PLAIN, commandKey());
        final Iterator<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking().getIterator();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.hasNext(), is(false));
    }

    @Test(expected = HystrixRuntimeException.class)
    public void shouldThrowExceptionOnHttpServerError() {
        // given
        final Response response = someResponse(500, "Some Server Error");
        final HttpClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = resilientContentFrom(mockClient, "/test", TEXT_PLAIN, commandKey());
        final BlockingObservable<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        content.single();
    }

    @Test(expected = HystrixRuntimeException.class)
    public void shouldThrowExceptionOnHttpClientError() {
        // given
        final Response response = someResponse(404, "Not Found");
        final HttpClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = resilientContentFrom(mockClient, "/test", TEXT_PLAIN, commandKey());
        final BlockingObservable<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        content.single();
    }

    @Test(expected = HystrixRuntimeException.class)
    public void shouldPassExceptions() {
        // given
        // this will throw an Exception (from Jersey Client) because /test is not an absolute URL.
        final HttpClient mockClient = someHttpClient(mock(Response.class), "/test");
        when(mockClient.get("/test", TEXT_PLAIN_TYPE)).thenReturn(fromCallable(() -> {
            throw new ConnectException("KA-WUMMMM!");
        }));
        // when
        final ContentProvider contentProvider = resilientContentFrom(mockClient, "/test", TEXT_PLAIN, commandKey());
        contentProvider.getContent(X, emptyParameters()).toBlocking().single();
    }

    private Response someResponse(final int status, final String body) {
        final Response response = mock(Response.class);
        when(response.readEntity(String.class)).thenReturn(body);
        when(response.getStatus()).thenReturn(status);
        when(response.getStatusInfo()).thenReturn(Statuses.from(status));
        return response;
    }

    private HttpClient someHttpClient(final Response response, final String uri) {
        final HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.get(uri, TEXT_PLAIN_TYPE)).thenReturn(just(response));
        return mockClient;
    }

    private static int counter = 0;
    private String commandKey() {
        return "test service #" + counter++;
    }
}