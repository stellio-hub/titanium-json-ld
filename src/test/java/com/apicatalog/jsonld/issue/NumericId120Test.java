/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apicatalog.jsonld.issue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.document.RdfDocument;
import com.apicatalog.jsonld.suite.JsonLdTestRunnerJunit;
import com.apicatalog.rdf.io.error.RdfWriterException;
import com.apicatalog.rdf.io.error.UnsupportedContentException;

import jakarta.json.JsonArray;
import jakarta.json.JsonStructure;

public class NumericId120Test {

    @Test
    public void testExpansion() throws JsonLdError, IOException, RdfWriterException, UnsupportedContentException {

        final Document document = readJsonDocument("issue120-in.json");
        
        final JsonArray result = JsonLd.expand(document).base("https://json-ld.org/playground/").numericId().get();
        
        assertNotNull(result);
                
        final JsonStructure expected = readJsonDocument("issue120-1-out.json").getJsonContent().orElse(null);
        
        assertNotNull(expected);
        
        assertTrue(JsonLdTestRunnerJunit.compareJson("expansion: numeric @id", result, expected));        
    }

    private final JsonDocument readJsonDocument(final String name) throws JsonLdError, IOException {
        try (final InputStream is = getClass().getResourceAsStream(name)) {
            return JsonDocument.of(is);
        }
    }
    
    private final RdfDocument readRdfDocument(final String name) throws JsonLdError, IOException {
        try (final InputStream is = getClass().getResourceAsStream(name)) {
            return RdfDocument.of(is);
        }
    }    
}
