package io.smallrye.openapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.junit.jupiter.api.Test;

class SmallRyeOpenAPIBuilderTest {

    @Test
    void testStaticFileLoadedFromClasspath() throws Exception {
        URL loaderRoot = getClass()
                .getClassLoader()
                .getResource("classloader/META-INF/openapi.yaml")
                .toURI()
                .resolve("..")
                .toURL();

        ClassLoader custom = new URLClassLoader(
                new URL[] { loaderRoot },
                Thread.currentThread().getContextClassLoader());

        SmallRyeOpenAPI result = SmallRyeOpenAPI.builder()
                .withApplicationClassLoader(custom)
                .enableModelReader(false)
                .enableStandardFilter(false)
                .enableAnnotationScan(false)
                .enableStandardStaticFiles(true)
                .build();

        OpenAPI model = result.model();
        assertEquals("Loaded from the class path", model.getInfo().getTitle());
    }

    @Test
    void testInvalidTypesInStaticFileDropped() {
        URL invalidResource = getClass()
                .getClassLoader()
                .getResource("io/smallrye/openapi/api/openapi-invalid-types.yaml");

        SmallRyeOpenAPI result = SmallRyeOpenAPI.builder()
                .enableModelReader(false)
                .enableStandardFilter(false)
                .enableAnnotationScan(false)
                .enableStandardStaticFiles(false)
                .withCustomStaticFile(() -> {
                    try {
                        return invalidResource.openStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .build();

        OpenAPI model = result.model();
        assertEquals("Invalid fields herein!", model.getInfo().getTitle());
        assertEquals("1.0.0", model.getInfo().getVersion());
        assertNull(model.getInfo().getContact());
        assertNull(model.getInfo().getLicense());
        assertNull(model.getInfo().getSummary());
        assertNull(model.getServers());
        assertNull(model.getTags());
        assertEquals(OASFactory.createPaths(), model.getPaths()); // empty
        assertNull(model.getComponents());
        assertNull(model.getWebhooks());
    }
}