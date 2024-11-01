package com.tear.upgrade.t1tot2upgrade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.CategoryService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtToken jwtToken;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    private ObjectMapper objectMapper;

    private String validMessage;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        validMessage = FileHelper.readFromFile("requests/category/Category.json");
    }

    @Test
    @WithMockUser()
    void whenUserLoggedInThenGetAllCategoriesSuccess() throws Exception {
        // given
        String validMessagesArray = FileHelper.readFromFile("requests/category/CategoryArray.json");
        List<CategoryDTO> categories = Arrays.asList(objectMapper.readValue(validMessagesArray, CategoryDTO[].class));

        // when
        when(categoryService.getAllExpenses(any(Pageable.class))).thenReturn(new PageImpl<>(categories));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Name 1"))
                .andExpect(jsonPath("$[1].name").value("Name 2"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[1].description").value("Description 2"));
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenDeleteCategoryByIdSuccess() throws Exception {
        // given
        Long categoryId = 1L;

        // when
        doNothing().when(categoryService).deleteCategoryById(categoryId);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/categories/{id}", categoryId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenSaveCategorySuccess() throws Exception {
        // given
        CategoryDTO categoryDTO = objectMapper.readValue(validMessage, CategoryDTO.class);

        // when
        when(categoryService.saveCategory(any(CategoryDTO.class))).thenReturn(categoryDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(validMessage)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Category"))
                .andExpect(jsonPath("$.description").value("Category Description"));
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenUpdateCategorySuccess() throws Exception {
        // given
        Long categoryId = 1L;
        String validMessage = FileHelper.readFromFile("requests/category/CategoryUpdated.json");
        CategoryDTO updatedCategoryDTO = objectMapper.readValue(validMessage, CategoryDTO.class);

        // when
        when(categoryService.updateCategory(eq(categoryId), any(CategoryDTO.class))).thenReturn(updatedCategoryDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMessage)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenDeleteAllCategoriesForUserSuccess() throws Exception {
        // when
        doNothing().when(categoryService).deleteAllCategoriesForUser(any(Pageable.class));

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/categories")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteAllCategoriesForUser(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void whenNoCategoriesFindForLoggedInUserThenThrowResourceNotFoundException() throws Exception {
        // when
        when(categoryService.getAllExpenses(any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("No categories found for the user"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/categories")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No categories found for the user")));
    }

    @Test
    @WithMockUser
    void whenCategoryNotFoundForLoggedInUserThenThrowResourceNotFoundException() throws Exception {
        // given
        Long categoryId = 1L;

        // when
        doThrow(new ResourceNotFoundException("Category is not found for id " + categoryId))
                .when(categoryService).deleteCategoryById(categoryId);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/categories/{id}", categoryId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Category is not found for id " + categoryId)));
        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    @Test
    @WithMockUser
    void whenCategoryNullThenThrowIllegalArgumentException() throws Exception {
        // when
        when(categoryService.saveCategory(any(CategoryDTO.class)))
                .thenThrow(new IllegalArgumentException("CategoryDTO cannot be null"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(validMessage)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("CategoryDTO cannot be null")));
    }

    @Test
    @WithMockUser
    void whenCategoryUpdateIdWrongThenThrowIllegalArgumentException() throws Exception {
        // given
        Long categoryId = 1L;

        // when
        when(categoryService.updateCategory(eq(categoryId), any(CategoryDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMessage)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input")));
    }

    @Test
    @WithMockUser
    void whenNoLoggedInUserFindThenThrowRuntimeException() throws Exception {

        //when
        doThrow(new RuntimeException("Unexpected error occurred"))
                .when(categoryService).deleteAllCategoriesForUser(any(Pageable.class));
        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/categories")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Unexpected error occurred")));
        verify(categoryService, times(1)).deleteAllCategoriesForUser(any(Pageable.class));
    }
}