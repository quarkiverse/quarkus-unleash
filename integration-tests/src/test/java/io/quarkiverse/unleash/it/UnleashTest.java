package io.quarkiverse.unleash.it;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
public class UnleashTest {

    //Configure the containers for the test
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //        RestAssured.filters(new ResponseLoggingFilter());
    }

    @Test
    public void testUnleashClient() {
        Response response = RestAssured.when()
                .get("/tests")
                .andReturn();

        Assertions.assertEquals(200, response.statusCode());
        Map<String, Boolean> flags = response.as(new TypeRef<Map<String, Boolean>>() {
        });
        Assertions.assertFalse(flags.get("quarkus-unleash-test-disabled"));
        Assertions.assertTrue(flags.get("quarkus-unleash-test-enabled"));
        Assertions.assertTrue(flags.get("toggle"));
        Assertions.assertTrue(flags.get("default-true"));
    }

    @Test
    public void testFeatureToggleAnnotation() {
        Response response = RestAssured.when()
                .get("/tests-anno")
                .andReturn();

        Assertions.assertEquals(200, response.statusCode());
        Map<String, Boolean> flags = response.as(new TypeRef<Map<String, Boolean>>() {
        });
        Assertions.assertFalse(flags.get("quarkus-unleash-test-disabled"));
        Assertions.assertTrue(flags.get("quarkus-unleash-test-enabled"));
        Assertions.assertTrue(flags.get("toggle"));
        Assertions.assertTrue(flags.get("default-true"));
    }

    @Test
    public void testVariant() {
        Response response = RestAssured.when()
                .get("/variant")
                .andReturn();

        Assertions.assertEquals(200, response.statusCode());

        System.out.println(response.asPrettyString());

        Map<String, Map<Object, Object>> flags = response.as(new TypeRef<Map<String, Map<Object, Object>>>() {
        });
        Map<Object, Object> iToggle = flags.get("i-toggle");
        Assertions.assertNotNull(iToggle);
        Assertions.assertEquals("toggle-variant", iToggle.get("name"));
        Map<Object, Object> payload = (Map<Object, Object>) iToggle.get("payload");
        Assertions.assertNotNull(payload);
        Assertions.assertEquals("json", payload.get("type"));
        Assertions.assertEquals("{\"value\":1,\"enabled\":true,\"text\":\"message\"}", payload.get("value"));

        Map<Object, Object> jsonType = flags.get("jsonType");
        Assertions.assertEquals("message", jsonType.get("text"));
        Assertions.assertEquals(1, jsonType.get("value"));
        Assertions.assertEquals(true, jsonType.get("enabled"));
    }
}
