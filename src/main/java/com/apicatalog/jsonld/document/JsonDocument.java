package com.apicatalog.jsonld.document;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.api.JsonLdErrorCode;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.rdf.RdfDataset;

public final class JsonDocument implements Document {

    private static final String PLUS_JSON = "+json";
        
    private final MediaType contentType;
    private final JsonStructure structure;
    private final String profile;

    private URI documentUrl;
    private URI contentUrl;
    
    private JsonDocument(final MediaType type, final String profile, final JsonStructure structue) {
        this.contentType = type;
        this.profile = profile;
        this.structure = structue;
    }

    /**
     * Create a new document from {@link JsonStructure}. Sets {@link MediaType#JSON} as the content type.
     *
     * @param structure representing parsed JSON content
     * @return {@link Document} representing JSON content
     */
    public static JsonDocument of(final JsonStructure structure) {
        return of(MediaType.JSON, structure);
    }
    
    /**
     * Create a new document from {@link JsonStructure}.
     *
     * @param contentType reflecting the provided {@link JsonStructure}, e.g. {@link MediaType#JSON_LD}, any JSON based media type is allowed
     * @param structure representing parsed JSON content
     * @return {@link Document} representing JSON content 
     */
    public static JsonDocument of(final MediaType contentType, final JsonStructure structure) {

        if (contentType == null) {
            throw new IllegalArgumentException("The provided JSON type is null.");
        }

        assertContentType(contentType);
        
        if (structure == null) {
            throw new IllegalArgumentException("The provided JSON structure is null.");
        }
        
        return new JsonDocument(MediaType.of(contentType.type(), contentType.subtype()), contentType.parameters().firstValue("profile").orElse(null), structure);
    }

    /**
     * Create a new document from content provided by {@link InputStream}. Sets {@link MediaType#JSON} as the content type.
     *
     * @param is representing parsed JSON content
     * @return {@link Document} representing JSON content
     */
    public static final JsonDocument of(final InputStream is)  throws JsonLdError {
        return of(MediaType.JSON, is);
    }

    /**
     * Create a new document from content provided by {@link InputStream}.
     *
     * @param contentType reflecting the provided {@link InputStream} content, e.g. {@link MediaType#JSON_LD}, any JSON based media type is allowed
     * @param is providing JSON content
     * @return {@link Document} representing JSON content
     * 
     * @throws JsonLdError
     */
    public static final JsonDocument of(final MediaType contentType, final InputStream is)  throws JsonLdError {
    
        assertContentType(contentType);

        if (is == null) {
            throw new IllegalArgumentException("The input stream parameter cannot be null.");
        }
        
        try (final JsonParser parser = Json.createParser(is)) {

            return doParse(contentType, parser);
            
        } catch (JsonException e) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e);
        }
    }

    /**
     * Create a new document from content provided by {@link Reader}. Sets {@link MediaType#JSON} as the content type.
     *
     * @param reader providing JSON content
     * @return {@link Document} representing JSON content
     */
    public static final JsonDocument of(final Reader reader)  throws JsonLdError {
        return of(MediaType.JSON, reader);
    }
    
    /**
     * Create a new document from content provided by {@link Reader}. 
     *
     * @param contentType reflecting the provided content, e.g. {@link MediaType#JSON_LD}, any JSON based media type is allowed
     * @param reader providing JSON content
     * @return {@link Document} representing JSON content
     * 
     * @throws JsonLdError
     */
    public static final JsonDocument of(final MediaType contentType, final Reader reader)  throws JsonLdError {
        
        assertContentType(contentType);
        
        if (reader == null) {
            throw new IllegalArgumentException("The reader parameter cannot be null.");
        }
        
        try (final JsonParser parser = Json.createParser(reader)) {

            return doParse(contentType, parser);
            
        } catch (JsonException e) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e);
        }
    }
    
    private static final JsonDocument doParse(final MediaType contentType, final JsonParser parser) throws JsonLdError {
        
        if (!parser.hasNext()) {
            throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "Nothing to read. Provided document is empty.");
        }

        parser.next();

        final JsonValue root = parser.getValue();

        final String profile = contentType.parameters().firstValue("profile").orElse(null);
            
        if (JsonUtils.isArray(root)) {
            return new JsonDocument(contentType, profile, root.asJsonArray());
        }

        if (JsonUtils.isObject(root)) {
            return new JsonDocument(MediaType.of(contentType.type(), contentType.subtype()), profile, root.asJsonObject());
        }

        throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, "JSON document's top level element must be JSON array or object.");
    }
    
    public static final boolean accepts(final MediaType contentType) {
        return contentType != null &&
                (MediaType.JSON_LD.match(contentType)
                || MediaType.JSON.match(contentType)
                || contentType.subtype().toLowerCase().endsWith(PLUS_JSON));        
    }
    
    private static final void assertContentType(final MediaType contentType) {
        if (!accepts(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported media type '" + contentType 
                    + "'. Supported content types are [" 
                    + MediaType.JSON_LD + ", " 
                    + MediaType.JSON  + ", +json]");
        }
    }
    
    @Override
    public Optional<JsonStructure> getJsonContent() {
        return Optional.of(structure);
    }

    @Override
    public MediaType getContentType() {
        return contentType;
    }

    @Override
    public URI getContextUrl() {
        return contentUrl;
    }

    @Override
    public void setContextUrl(URI contextUrl) {
        this.contentUrl = contextUrl;
    }

    @Override
    public URI getDocumentUrl() {
        return documentUrl;
    }

    @Override
    public void setDocumentUrl(URI documentUrl) {
        this.documentUrl = documentUrl;
    }

    @Override
    public Optional<String> getProfile() {
        return Optional.ofNullable(profile);
    }

    @Override
    public Optional<RdfDataset> getRdfContent() {
        return Optional.empty();
    }
}
