/*

 Paulo Henrique Araujo Munhoz -  CT1004879

*/

import com.github.javafaker.Faker;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GroceryStore {

    private static final String BASE_URL = "https://simple-grocery-store-api.glitch.me";
    private static final String STATUS_ENDPOINT = "status";
    private static final String PRODUCTS_ENDPOINT = "products";
    private static final String API_CLIENTS_ENDPOINT = "api-clients/";
    private static final String CART_ENDPOINT = "carts";
    private static final String ORDERS_ENDPOINT = "orders";

    private RequestSpecification request;
    private Integer ProductID;
    private Integer ProductAuxID;
    private int itemID;
    private int totalProducts;
    private int totalOrders;
    private String accessToken;
    private String cartID;
    private String customerName;
    private String orderID;


    @BeforeClass
    public void setUp() {
        request = given().baseUri(BASE_URL);
        accessToken = null;
        cartID = null;
    }

    @BeforeMethod
    public void setUpMethod() {
        request = given().baseUri(BASE_URL);
    }

    private Response getResponse(String endpoint) {
        return request.get(endpoint);
    }

    @Test(groups = {"status", "priority_1"})
    public void verifyWelcomeMessage() {
        getResponse(BASE_URL)
                .then()
                .statusCode(200)
                .body("message", equalTo("Simple Grocery Store API."));
    }

    @Test(groups = {"status", "priority_1"})
    public void statusPage() {
        Response response = getResponse(STATUS_ENDPOINT);
        response
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test(groups = {"products", "priority_1"})
    public void allProductsStatus200() {
        Response response = getResponse(PRODUCTS_ENDPOINT);
        response
                .then()
                .statusCode(200)
                .body("resul", hasSize(greaterThan(0)));

        List<Map<String, Object>> products = response.as(new TypeRef<List<Map<String, Object>>>() {
        });
        products.stream().filter(product -> (Boolean) product.get("inStock")).limit(2).forEach(product -> {
            if (ProductID == null) {
                ProductID = ((Number) product.get("id")).intValue();
            } else if (ProductAuxID == null) {
                ProductAuxID = ((Number) product.get("id")).intValue();
            }
        });
        totalProducts = products.size();
    }

    @Test(dependsOnMethods = {"allProductsStatus200"}, groups = {"products", "priority_1"})
    public void allProductsLimit() {
        request
                .queryParam("resul", totalProducts)
                .get(PRODUCTS_ENDPOINT)
                .then()
                .statusCode(200);
    }

    @Test(dependsOnMethods = {"allProductsStatus200"}, groups = {"products", "priority_1"})
    public void singleProduct() {
        Response response = getResponse(PRODUCTS_ENDPOINT + "/" + ProductID);
        response
                .then()
                .statusCode(200)
                .body("id", equalTo(ProductID));
        Assert.assertTrue(response.jsonPath().getInt("current-stock") > 0, "Stock should be greater than 0");
    }

    @Test(groups = {"products", "priority_1"})
    public void noProduct() {
        getResponse(PRODUCTS_ENDPOINT + "/9000")
                .then()
                .statusCode(404)
                .body("error", equalTo("No product with id 9000."));
    }

    // Group 3: Cart-related Tests
    @Test(groups = {"cart", "priority_1"})
    public void newCart() {
        Response response = request.post(CART_ENDPOINT);
        response
                .then()
                .statusCode(201)
                .body("$", hasKey("cartId"));
        cartID = response.jsonPath().getString("cartId");
    }

    @Test(dependsOnMethods = {"newCart"}, groups = {"cart", "priority_1"})
    public void addItemToCart() {
        JSONObject body = new JSONObject();
        body.put("productId", ProductID);
        body.put("quantity", 1);

        Response response = request.pathParam("cartid", cartID).contentType(ContentType.JSON).body(body.toString()).post(CART_ENDPOINT + "/{cartid}/items");
        response
                .then()
                .statusCode(201);
        itemID = response.jsonPath().getInt("itemId");
    }

    @Test(dependsOnMethods = {"addItemToCart"}, groups = {"cart", "priority_1"})
    public void updateItemsQuantityOfCart() {
        JSONObject body = new JSONObject();
        body.put("quantity", 2);

        request
                .pathParam("cartid", cartID)
                .pathParam("itemid", itemID)
                .contentType(ContentType.JSON)
                .body(body.toString())
                .patch(CART_ENDPOINT + "/{cartid}/items/{itemid}")
                .then()
                .statusCode(204);
    }

    @Test(dependsOnMethods = {"addItemToCart", "updateItemsQuantityOfCart"}, groups = {"cart", "priority_1"})
    public void replaceItems() {
        JSONObject body = new JSONObject();
        body.put("productId", ProductAuxID);
        body.put("quantity", 2);

        request
                .pathParam("cartid", cartID)
                .pathParam("itemid", itemID)
                .contentType(ContentType.JSON)
                .body(body.toString())
                .put(CART_ENDPOINT + "/{cartid}/items/{itemid}")
                .then()
                .statusCode(204);
    }

    @Test(dependsOnMethods = {"replaceItems", "addItemToCart", "updateItemsQuantityOfCart"}, groups = {"cart", "priority_1"})
    public void deleteItemFromCart() {
        request
                .pathParam("cartid", cartID)
                .pathParam("itemid", itemID)
                .delete(CART_ENDPOINT + "/{cartid}/items/{itemid}")
                .then()
                .statusCode(204);
    }

    @Test(dependsOnMethods = {"deleteItemFromCart"}, groups = {"cart", "priority_1"})
    public void itemsOfCartWithChangedQuantity() {
        request
                .pathParam("cartid", cartID)
                .get(CART_ENDPOINT + "/{cartid}/items")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test(groups = {"auth", "priority_2"})
    public void AccessToken() {
        Faker faker = new Faker();
        JSONObject body = new JSONObject();
        body.put("clientName", faker.name().firstName());
        body.put("clientEmail", faker.internet().emailAddress());

        Response response = request
                .contentType(ContentType.JSON)
                .body(body.toString())
                .post(API_CLIENTS_ENDPOINT);

        response
                .then()
                .statusCode(201)
                .body("$", hasKey("accessToken"));
        accessToken = response.jsonPath().getString("accessToken");
    }

    @Test(groups = {"auth", "priority_2"})
    public void AccessToken_InvalidCase() {
        JSONObject body = new JSONObject();
        body.put("clientName", "");
        body.put("clientEmail", "");

        request
                .contentType(ContentType.JSON)
                .body(body.toString())
                .post(API_CLIENTS_ENDPOINT)
                .then()
                .statusCode(400);
    }

    @Test(groups = {"cart", "priority_2"}, dependsOnGroups = {"priority_1"})
    public void newOrder() {
        Response response = request.post(CART_ENDPOINT);
        response
                .then()
                .statusCode(201)
                .body("$", hasKey("cartId"));
        cartID = response.jsonPath().getString("cartId");
    }

    @Test(dependsOnGroups = {"priority_1"}, dependsOnMethods = {"newOrder"}, groups = {"cart", "priority_2"})
    public void AddToCartOrder() {
        JSONObject body = new JSONObject();
        body.put("productId", ProductID);
        body.put("quantity", 1);

        Response response = request.pathParam("cartid", cartID).contentType(ContentType.JSON).body(body.toString()).post(CART_ENDPOINT + "/{cartid}/items");
        response
                .then()
                .statusCode(201);
        itemID = response.jsonPath().getInt("itemId");
    }

    @Test(dependsOnMethods = {"newOrder", "AddToCartOrder"}, dependsOnGroups = {"priority_1"}, groups = {"cart", "priority_2"})
    public void updateItemsQuantityOfCartToOrder() {
        JSONObject body = new JSONObject();
        body.put("quantity", 2);

        request
                .pathParam("cartid", cartID)
                .pathParam("itemid", itemID)
                .contentType(ContentType.JSON)
                .body(body.toString())
                .patch(CART_ENDPOINT + "/{cartid}/items/{itemid}")
                .then()
                .statusCode(204);
    }

    @Test(dependsOnGroups = {"priority_1"}, dependsOnMethods = {"newOrder", "AddToCartOrder", "updateItemsQuantityOfCartToOrder"}, groups = {"cart", "priority_2"})
    public void alterOrder() {
        JSONObject body = new JSONObject();
        body.put("productId", ProductAuxID);
        body.put("quantity", 2);

        request
                .pathParam("cartid", cartID)
                .pathParam("itemid", itemID)
                .contentType(ContentType.JSON)
                .body(body.toString())
                .put(CART_ENDPOINT + "/{cartid}/items/{itemid}")
                .then()
                .statusCode(204);
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"})
    public void allOrder() {

        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .get(ORDERS_ENDPOINT);

        response
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = "allOrder")
    public void startOrder() {
        Faker faker = new Faker();
        JSONObject body = new JSONObject();
        customerName = faker.name().fullName();
        body.put("cartId", cartID);
        body.put("customerName", customerName);
        body.put("orderComment", "");
        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(body.toString())
                .post(ORDERS_ENDPOINT);

        response
                .then()
                .statusCode(201)
                .body("created", equalTo(true))
                .body("$", hasKey("orderId"));
        orderID = response.jsonPath().getString("orderId");
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder"})
    public void oneOrder() {
        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .pathParams("orderId", orderID)
                .get(ORDERS_ENDPOINT + "/{orderId}");

        response
                .then()
                .statusCode(200)
                .body("id", equalTo(orderID));

        List<Map<String, Object>> items = response.jsonPath().getList("items");
        Assert.assertNotNull(items, "List of items is null");
        Assert.assertFalse(items.isEmpty(), "List of items is empty");

        for (Map<String, Object> item : items) {
            Integer itemId = (Integer) item.get("id");
            Assert.assertEquals((int) itemId, itemID, "ItemID is not equivalent");

            Integer productId = (Integer) item.get("productId");
            Assert.assertEquals(productId, ProductAuxID, "productID is not equivalent");

            Integer quantity = (Integer) item.get("quantity");
            Assert.assertEquals(quantity, 2, "Quantity is not equivalent");

        }

        String customer = response.jsonPath().getString("customerName");
        Assert.assertEquals(customer, customerName, "Customer name is not equivalent");
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder"})
    public void allOrderStarted() {
        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .get(ORDERS_ENDPOINT);

        response
                .then()
                .statusCode(200)
                .body("", hasSize(greaterThan(0)));

        List<Map<String, Object>> jsonData = response.jsonPath().getList(""); // Adjust the path as needed
        totalOrders = jsonData.size();

        assert totalOrders > 0 : "No orders retrieved";

        List<Map<String, Object>> matchingOrders = new ArrayList<>();
        for (Map<String, Object> order : jsonData) {
            if (order.get("id").equals(orderID)) {
                matchingOrders.add(order);
            }
        }

        assert !matchingOrders.isEmpty() : "Order ID not found in orders list!";

        Map<String, Object> matchingOrder = matchingOrders.getFirst();
        List<Map<String, Object>> matchingItems = (List<Map<String, Object>>) matchingOrder.get("items");

        assert !matchingItems.isEmpty() : "Matching orders list is empty";

        // Verify returned order
        assert matchingOrder.get("id").equals(orderID) : "Order ID is not equivalent";
        assert matchingItems.getFirst().get("id").equals(itemID) : "Item ID is not equivalent";
        assert matchingItems.getFirst().get("productId").equals(ProductAuxID) : "Product ID is not equivalent";
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder", "oneOrder", "allOrderStarted"})
    public void customersNameOrder() {
        Faker faker = new Faker();
        JSONObject body = new JSONObject();
        body.put("customerName", faker.name().fullName());

        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(body.toString())
                .pathParams("orderId", orderID)
                .patch(ORDERS_ENDPOINT + "/{orderId}");

        response
                .then()
                .statusCode(204);
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder", "oneOrder", "allOrderStarted"})
    public void customersOrderComment() {
        Faker faker = new Faker();
        JSONObject body = new JSONObject();
        body.put("comment", faker.rockBand().name());

        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .contentType(ContentType.JSON)
                .body(body.toString())
                .pathParams("orderId", orderID)
                .patch(ORDERS_ENDPOINT + "/{orderId}");

        response
                .then()
                .statusCode(204);
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder", "oneOrder", "allOrderStarted", "customersOrderComment", "customersNameOrder"})
    public void orderWithChangedinfo() {
        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .pathParams("orderId", orderID)
                .get(ORDERS_ENDPOINT + "/{orderId}");

        response
                .then()
                .statusCode(200)
                .body("id", equalTo(orderID));

        List<Map<String, Object>> items = response.jsonPath().getList("items");
        Assert.assertNotNull(items, "List of items is null");
        Assert.assertFalse(items.isEmpty(), "List of items is empty");

        for (Map<String, Object> item : items) {
            Integer itemId = (Integer) item.get("id");
            Assert.assertEquals((int) itemId, itemID, "ItemID is not equivalent");

            Integer productId = (Integer) item.get("productId");
            Assert.assertEquals(productId, ProductAuxID, "productID is not equivalent");

            Integer quantity = (Integer) item.get("quantity");
            Assert.assertEquals(quantity, 2, "Quantity is not equivalent");

        }

        String customer = response.jsonPath().getString("customerName");
        Assert.assertNotEquals(customer, customerName, "Customer name is the same");
        customerName = response.jsonPath().getString("customerName");

        String comment = response.jsonPath().getString("comment");
        Assert.assertNotEquals(comment, "", "Comment is the same");
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder", "oneOrder", "allOrderStarted", "customersOrderComment", "customersNameOrder", "orderWithChangedinfo"})
    public void eraseOrder() {
        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .pathParams("orderId", orderID)
                .delete(ORDERS_ENDPOINT + "/{orderId}");

        response
                .then()
                .statusCode(204);

    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder", "oneOrder", "allOrderStarted", "customersOrderComment", "customersNameOrder", "orderWithChangedinfo", "eraseOrder"})
    public void OrdersDeletedOnAllOrders() {
        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .get(ORDERS_ENDPOINT);

        response
                .then()
                .statusCode(200);

        response
                .then()
                .body(equalTo("[]"));
    }

    @Test(groups = {"orders", "priority_3"}, dependsOnGroups = {"auth", "priority_2"}, dependsOnMethods = {"startOrder", "oneOrder", "allOrderStarted", "customersOrderComment", "customersNameOrder", "orderWithChangedinfo", "eraseOrder"})
    public void retrieveDeletedOrder() {
        Response response = request
                .header("Authorization", "Bearer " + accessToken)
                .pathParams("orderId", orderID)
                .get(ORDERS_ENDPOINT + "/{orderId}");

        response
                .then()
                .statusCode(404);

        String expectedError = "No order with id " + orderID + ".";
        response.then().body("error", equalTo(expectedError));
    }
}
