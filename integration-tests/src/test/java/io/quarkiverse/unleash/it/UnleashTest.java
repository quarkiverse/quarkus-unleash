package io.quarkiverse.unleash.it;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.util.*;

import org.junit.jupiter.api.*;

import io.getunleash.*;
import io.quarkiverse.unleash.InjectUnleash;
import io.quarkiverse.unleash.InjectUnleashAdmin;
import io.quarkiverse.unleash.UnleashAdmin;
import io.quarkiverse.unleash.UnleashTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

@QuarkusTest
@QuarkusTestResource(UnleashTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnleashTest {

    //Configure the containers for the test
    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //        RestAssured.filters(new ResponseLoggingFilter());
    }

    @InjectUnleashAdmin
    UnleashAdmin admin;

    @InjectUnleash
    Unleash client;

    @Test
    @Order(1)
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
    @Order(2)
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
        Assertions.assertTrue(flags.get("toggle-from-constructor-injection"));
    }

    @Test
    @Order(3)
    public void testVariant() {
        Response response = RestAssured.when()
                .get("/variant")
                .andReturn();

        Assertions.assertEquals(200, response.statusCode());

        Map<String, Map<Object, Object>> flags = response.as(new TypeRef<Map<String, Map<Object, Object>>>() {
        });
        Map<Object, Object> iToggle = flags.get("i-toggle");
        Assertions.assertNotNull(iToggle);
        Assertions.assertEquals("toggle-variant", iToggle.get("name"));
        @SuppressWarnings("unchecked")
        Map<Object, Object> payload = (Map<Object, Object>) iToggle.get("payload");
        Assertions.assertNotNull(payload);
        Assertions.assertEquals("json", payload.get("type"));
        Assertions.assertEquals("{\"value\":1,\"enabled\":true,\"text\":\"message\"}", payload.get("value"));

        Map<Object, Object> jsonType = flags.get("jsonType");
        Assertions.assertEquals("message", jsonType.get("text"));
        Assertions.assertEquals(1, jsonType.get("value"));
        Assertions.assertEquals(true, jsonType.get("enabled"));

        Assertions.assertNotNull(flags.get("toggle-from-constructor-injection"));
    }

    @Test
    @Order(4)
    public void testAdminClient() {

        admin.toggleOff("toggle");
        admin.toggleOn("quarkus-unleash-test-disabled");

        // wait for change
        await().atMost(7, SECONDS)
                .pollInterval(2, SECONDS)
                .until(() -> client.isEnabled("quarkus-unleash-test-disabled"));

        // wait for change
        await().atMost(7, SECONDS)
                .pollInterval(2, SECONDS)
                .until(() -> RestAssured.when()
                        .get("/tests/quarkus-unleash-test-disabled")
                        .andReturn().as(Boolean.class));

        Response response = RestAssured.when()
                .get("/tests-anno")
                .andReturn();

        Assertions.assertEquals(200, response.statusCode());
        Map<String, Boolean> flags = response.as(new TypeRef<Map<String, Boolean>>() {
        });
        Assertions.assertTrue(flags.get("quarkus-unleash-test-disabled"));
        Assertions.assertTrue(flags.get("quarkus-unleash-test-enabled"));
        Assertions.assertFalse(flags.get("toggle"));
        Assertions.assertTrue(flags.get("default-true"));

        admin.toggleOff("quarkus-unleash-test-disabled");
        admin.toggleOn("toggle");

    }

    @Test
    public void testContext() {
        Set<String> expected = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            UnleashContext context = UnleashContext.builder()
                    .userId("" + i)
                    .environment("default")
                    .addProperty("betaEnabled", "true")
                    .build();
            boolean enabled = client.isEnabled("rollout", context);
            System.out.println("rollout: " + i + " value: " + enabled);
            if (enabled) expected.add("" + i);
        }

        Assertions.assertTrue(expected.size() > 2, "At least 3 expected");
        Assertions.assertTrue(expected.size() < 8, "At most 8 expected");

        for (int i = 0; i < 10; i++) {
            testContext("" + i, expected.contains("" + i));
        }
    }

    public void testContext(String userId, boolean expected) {

        Response response = RestAssured.when()
                .get("/context?betaEnabled=true&userId=" + userId)
                .andReturn();

        Assertions.assertEquals(200, response.statusCode());

        Map<String, Object> flags = response.as(new TypeRef<Map<String, Object>>() {
        });

        Assertions.assertEquals("default", flags.get("request-environment"));
        Assertions.assertEquals("default", flags.get("application-environment"));
        Assertions.assertEquals(userId, flags.get("request-userId"));
        Assertions.assertNull(flags.get("application-userId"));
        Assertions.assertEquals(false, flags.get("application"));
        Assertions.assertEquals(expected, flags.get("request"));
    }
}
