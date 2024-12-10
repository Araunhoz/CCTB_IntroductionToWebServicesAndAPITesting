import io.restassured.common.mapper.TypeRef;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
import org.testng.annotations.Test;

import java.awt.print.Book;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;

import static org.hamcrest.Matchers.*;

public class RestJava {

    private String baseurl = "https://simple-books-api.glitch.me";
    private RequestSpecification request = given().baseUri(baseurl);
    //@org.testng.annotations.Test

    @Test

    public void GetBookTwo() {
        given().baseUri(baseurl)
                .pathParam("bookId", bookId)
        .when()
                .get("/Books/{bookId}")
        .then()
                .body("id", isA(Integer.class))
                .body("name", isA(String.class))
                .body("available", isA(Boolean.class));
    }

    @Test

    public void GetAllBooks() {
        var response = given()
                .baseUri(baseurl)
        when()
                .get()
    }


    @Test
    public void StatusReturns200() {
        given()
                .baseUri(baseurl)
                .when()
                .get("/status")
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void StatusReturnsTwoHundred() {
        request.get("/status").then().statusCode(200);
    }

    @Test
    public void StatusReturnJSON() {
        given()
                .baseUri(baseurl)
                .when()
                .get("/status")
                .then()
                .assertThat().contentType("application/json");
    }

    @Test
    public void StatusReturnsStatusProperty() {
        given()
                .baseUri(baseurl)
                .when()
                .get("/status")
                .then()
                .body("$", hasKey("status"));
    }

    @Test
    public void StatusReturnsString() {
        given()
                .baseUri(baseurl)
                .when()
                .get("/status")
                .then()
                .body("status", isA(String.class));
    }
    @Test
    public void StatusMatchesOK() {
        given()
                .baseUri(baseurl)
                .when()
                .get("/status")
                .then()
                .body("status", equalTo("OK"));
    }

    @Test
    public void StatusIsOK() {
        var response = request.get("/status");

        // Grab the response as a String
        var responseString = response.asString();
        //System.out.println(responseString);

        // Convert responseString to a JSONPath object to inspect
        var jsonpath = JsonPath.from(responseString);
        String status = jsonpath.getString("status");
        //System.out.println(status);

        // Manually create an assert
        assert(status.equals("OK"));
    }

    /*
    @Test
    public void GetAllBooks() {
        var response = request.get("/books");
        var books = response.as(new TypeRef);

        for (var book : books) {
            System.out.println(book.get("title"));
        }
    }
    */
}





























//let books = pm.response.json()
//
//let totalBookCount = pm.environment.get('totalBookCount')
//let bookLimit = pm.environment.get('bookLimit')
//
//if (bookLimit > totalBookCount) {
//bookLimit = totalBookCount
//}
//
//        pm.test('Check book limit', () => {
//        pm.expect(books.length).equals(bookLimit)
//})