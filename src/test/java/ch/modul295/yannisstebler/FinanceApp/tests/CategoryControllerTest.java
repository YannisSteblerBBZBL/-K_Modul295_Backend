package ch.modul295.yannisstebler.FinanceApp.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.modul295.yannisstebler.financeapp.model.Category;
import ch.modul295.yannisstebler.financeapp.repository.CategoryRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();

        categoryRepository.deleteAll();

        Category foodCategory = new Category();
        foodCategory.setName("Food");
        foodCategory.setDescription("Category for food-related expenses");
        categoryRepository.save(foodCategory);

        Category transportCategory = new Category();
        transportCategory.setName("Transport");
        transportCategory.setDescription("Category for transportation-related expenses");
        categoryRepository.save(transportCategory);
    }

    @Test
    public void testGetAllCategories() throws Exception {
        String accessToken = obtainAccessToken();

        mockMvc.perform(get("/categories")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].name").value("Transport"));
    }

    @Test
    public void testCreateCategory() throws Exception {
        String accessToken = obtainAccessToken();

        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Category Description");

        mockMvc.perform(post("/categories")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(category.getName()))
                .andExpect(jsonPath("$.description").value(category.getDescription()));
    }

    @Test
    public void testUpdateCategory() throws Exception {
        String accessToken = obtainAccessToken();

        // Hole eine existierende Kategorie aus der DB
        Category existingCategory = categoryRepository.findAll().get(0);
        existingCategory.setName("Updated Category");
        existingCategory.setDescription("Updated Description");

        mockMvc.perform(put("/categories/" + existingCategory.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    public void testDeleteCategory() throws Exception {
        String accessToken = obtainAccessToken();

        // Hole eine existierende Kategorie aus der DB
        Category existingCategory = categoryRepository.findAll().get(0);

        mockMvc.perform(delete("/categories/" + existingCategory.getId())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categories/" + existingCategory.getId())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    private String obtainAccessToken() {
        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=FinanceApp&" +
                "grant_type=password&" +
                "scope=openid profile roles offline_access&" +
                "client_secret=SytRPZuvq1uU7qM8fozRbUGKaYKosioo&" +
                "username=test&" +
                "password=test";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = rest.postForEntity("http://localhost:8080/realms/financeApp/protocol/openid-connect/token", entity, String.class);

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resp.getBody()).get("access_token").toString();
    }
}
