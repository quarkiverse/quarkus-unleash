package io.quarkiverse.unleash;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class UnleashAdmin {

    private static final String PRJ = "default";

    private static final String ENV = "default";

    RequestSpecification request;

    public UnleashAdmin(String adminUrl) {
        request = given()
                .baseUri(adminUrl)
                .request().contentType(ContentType.JSON);
    }

    public void debug() {
        request = request.log().all();
    }

    public void toggleOn(String feature) {
        toggleOn(PRJ, feature, ENV);
    }

    public void toggleOn(String project, String feature, String environment) {
        toggle(project, feature, environment, "on");
    }

    public void toggleOff(String feature) {
        toggleOff(PRJ, feature, ENV);
    }

    public void toggleOff(String project, String feature, String environment) {
        toggle(project, feature, environment, "off");
    }

    private void toggle(String project, String feature, String environment, String status) {
        request.post(urlToggle(project, feature, environment, status))
                .then()
                .statusCode(200);
    }

    private String urlProjects() {
        return "/admin/projects";
    }

    private String urlProject(String project) {
        return urlProjects() + "/" + project;
    }

    private String urlFeatures(String project) {
        return urlProject(project) + "/features";
    }

    private String urlFeature(String project, String feature) {
        return urlFeatures(project) + "/" + feature;
    }

    private String urlEnvironments(String project, String feature) {
        return urlFeature(project, feature) + "/environments";
    }

    private String urlEnvironment(String project, String feature, String environment) {
        return urlEnvironments(project, feature) + "/" + environment;
    }

    private String urlToggle(String project, String feature, String environment, String status) {
        return urlEnvironment(project, feature, environment) + "/" + status;
    }
}
