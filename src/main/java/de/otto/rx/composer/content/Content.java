package de.otto.rx.composer.content;

import java.time.LocalDateTime;

/**
 * Possible Content fragment for a {@link Position} on a page.
 * <p>
 *     Content is retrieved using {@link de.otto.rx.composer.providers.ContentProvider}s.
 * </p>
 * <p>
 *     If multiple content items are available for a position, the {@link de.otto.rx.composer.providers.ContentProvider}
 *     that is fetching the content will decide, which content is selected for the position.
 * </p>
 *
 */
public interface Content {


    /**
     * Content availability
     */
    public enum Availability {
        /** Content is available and not empty. */
        AVAILABLE,
        /** Content is empty, but no error occured. */
        EMPTY,
        /** An error occured. The content {@link #getBody() body} may contain an error response. */
        ERROR;

    }

    /**
     * Returns the source of the Content.
     * <p>
     *     For HTTP Content, this is the URL. In other cases, some other unique source key should be used,
     *     as this method is used to track the behaviour during execution.
     * </p>
     * @return source identifier
     */
    String getSource();

    /**
     * The content position inside of the {@link de.otto.rx.composer.Plan}
     *
     * @return Position
     */
    Position getPosition();

    /**
     *
     * @return true, if content is available and not empty, false otherwise.
     */
    boolean hasContent();

    /**
     * The body of the content element, as returned from the {@link de.otto.rx.composer.providers.ContentProvider}
     *
     * @return body or empty String
     */
    String getBody();

    /**
     * Return meta-information about the content returned from a {@link de.otto.rx.composer.providers.ContentProvider}.
     * <p>
     *     For HttpContent, the headers are the HTTP response headers returned from the called service.
     * </p>
     *
     * @return response headers.
     */
    Headers getHeaders();

    /**
     * The creation time stamp of the content element.
     * <p>
     *     Primarily used for logging purposes.
     * </p>
     *
     * @return created ts
     */
    LocalDateTime getCreated();

    /**
     * The availability of the content.
     *
     * @return availability
     */
    Availability getAvailability();
}
