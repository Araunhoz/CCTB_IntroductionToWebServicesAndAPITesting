//import org.testng.annotations.Test;
//import static io.restassured.RestAssured.*;
//
//public class Rest {
//
//    private String baseUrl= "https://simple-books-api.glitch.me";
//    //private var request =  given().baseUrl(baseUrl);
//
//    @Test
//    public void StatusReturn200(){
//        given()
//                .baseUri(baseUrl)
//        .when()
//                .get("/status")
//                .then()
//                .assertThat().statusCode(200);
//
//    }
//
//    @Test
//    public void StatusToHundread(){
//       /*// var request =  given().baseUrl(baseUrl);
//        var response = request.when().get("status");
//        response.then().assertThat().statuscode(200);
//        */
//        given()
//                .baseUri("https://simple-books-api.glitch.me")
//                .when()
//                .get("/status")
//                .then()
//                .assertThat().statusCode(200);
//
//    }
//
//    @Test
//    public void StatusReturnJson(){
//        given()
//                .baseUri("https://simple-books-api.glitch.me")
//                .when()
//                .get("/status")
//                .then()
//                .assertThat().contentType("application/json");
//
//    }
//
//    @Test
//    public void StatusIsOn(){
//        var response = request.get("/status");
////        var responseString = response.asString();
////        System.out.println(responseString);
////
////        var  jsonPath = JasonPaAth.from(responseString);
////        var status
//    }
//
//    @Test
//    public void StatusMatchesOk(){
//        given().RequestSpecification
//                .baseUrl(baseUrl)
//        .when()
//                .get("/status")
//        .then().ValiableResponse
//                .body("status", equalsTo("OK"));
//    }
//}
