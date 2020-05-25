package com.apicatalog.jsonld.context;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.api.JsonLdOptions;
import com.apicatalog.jsonld.compaction.UriCompactionBuilder;
import com.apicatalog.jsonld.expansion.UriExpansionBuilder;
import com.apicatalog.jsonld.expansion.ValueExpansionBuilder;
import com.apicatalog.jsonld.grammar.DirectionType;
import com.apicatalog.jsonld.grammar.Version;

/**
 * A context that is used to resolve terms while the processing algorithm is
 * running.
 * 
 */
public final class ActiveContext {

    // the active term definitions which specify how keys and values have to be
    // interpreted
    Map<String, TermDefinition> terms;

    // the current base IRI
    URI baseUri;

    // the original base URL
    URI baseUrl;

    ActiveContext inverseContext;

    // an optional previous context, used when a non-propagated context is defined.
    ActiveContext previousContext;

    // an optional vocabulary mapping
    String vocabularyMapping;

    // an optional default language
    String defaultLanguage;

    // an optional default base direction ("ltr" or "rtl")
    DirectionType defaultBaseDirection;

    final JsonLdOptions options;

    public ActiveContext(final URI baseUri, final URI baseUrl, JsonLdOptions options) {
        this.baseUri = baseUri;
        this.baseUrl = baseUrl;
        this.terms = new LinkedHashMap<>();
        this.options = options;
    }

    public ActiveContext(final URI baseUri, final URI baseUrl, final ActiveContext previousContext, final JsonLdOptions options) {
        this.baseUri = baseUri;
        this.baseUrl = baseUrl;
        this.previousContext = previousContext;
        this.terms = new LinkedHashMap<>();
        this.options = options;
    }

    // copy constructor
    public ActiveContext(final ActiveContext origin) {
        this.terms = new LinkedHashMap<>(origin.terms);
        this.baseUri = origin.baseUri;
        this.baseUrl = origin.baseUrl;
        this.inverseContext = origin.inverseContext;
        this.previousContext = origin.previousContext;
        this.vocabularyMapping = origin.vocabularyMapping;
        this.defaultLanguage = origin.defaultLanguage;
        this.defaultBaseDirection = origin.defaultBaseDirection;
        this.options = origin.options;
    }

    public boolean containsTerm(String term) {
        return terms.containsKey(term);
    }

    public boolean containsProtectedTerm() {
        return terms.values().stream().anyMatch(d -> d.protectedFlag);
    }

    public TermDefinition removeTerm(String term) {

        if (terms.containsKey(term)) {
            TermDefinition def = terms.get(term);
            terms.remove(term);
            return def;
        }

        return null;
    }

    public void setTerm(String term, TermDefinition definition) {
        terms.put(term, definition);
    }

    public TermDefinition getTerm(String value) {
        return terms.get(value);
    }

    public DirectionType getDefaultBaseDirection() {
        return defaultBaseDirection;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public String getVocabularyMapping() {
        return vocabularyMapping;
    }

    public boolean inMode(Version version) {
        return options.getProcessingMode() != null && options.getProcessingMode().equals(version);
    }

    public boolean hasPreviousContext() {
        return previousContext != null;
    }

    public ActiveContext getPreviousContext() {
        return previousContext;
    }
    
    public URI getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUri(URI baseUri) {
        this.baseUri = baseUri;
    }

    public ActiveContext getInverseContext() {
        return inverseContext;
    }
    
    public void setInverseContext(ActiveContext inverseContext) {
        this.inverseContext = inverseContext;
    }
    
    public Map<String, TermDefinition> getTermsMapping() {
        return terms;
    }

    public ValueExpansionBuilder expandValue(final JsonValue element, final String activeProperty) throws JsonLdError {
        return ValueExpansionBuilder.with(this, element, activeProperty);
    }

    public UriExpansionBuilder expandUri(final String value) {
        return UriExpansionBuilder.with(this, value);
    }

    public TermDefinitionBuilder createTerm(JsonObject localContext, String term, Map<String, Boolean> defined) {
        return TermDefinitionBuilder.with(this, localContext, term, defined);
    }

    public ActiveContextBuilder create(final JsonValue localContext, final URI base) {
        return ActiveContextBuilder.with(this, localContext, base, options);
    }

    public UriCompactionBuilder compacttUri(final String value) {
        return UriCompactionBuilder.with(this, value);
    }
    
    public InverseContextBuilder createInverse() {
        return InverseContextBuilder.with(this);
    }
}
