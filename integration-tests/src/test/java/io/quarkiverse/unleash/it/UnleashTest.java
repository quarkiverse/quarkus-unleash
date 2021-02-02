package io.quarkiverse.unleash.it;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;

@QuarkusTest
@QuarkusTestResource(UnleashResource.class)
public class UnleashTest {

    static {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    public void findTest() {
        Response response = RestAssured.when()
                .get("/tests")
                .andReturn();

        Assertions.assertEquals(200, response.statusCode());
        Boolean flag = response.as(Boolean.class);
        Assertions.assertFalse(flag);
    }
}
