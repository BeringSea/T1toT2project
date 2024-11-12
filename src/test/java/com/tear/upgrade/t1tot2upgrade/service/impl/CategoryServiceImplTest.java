package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.CategoryRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private UserService userService;

    @Mock
    private User loggedInUser;

    @Mock
    private CategoryRepository categoryRepository;

    private ObjectMapper objectMapper;

    private CategoryDTO categoryDTOMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.getLoggedInUser()).thenReturn(mockUser);
        objectMapper = new ObjectMapper();
        categoryDTOMock = mock(CategoryDTO.class);
    }

    @Test
    void whenUserLoggedInThenGetAllCategoriesSuccess() throws IOException {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        String validMessagesArray = FileHelper.readFromFile("requests/category/CategoryArray.json");
        List<Category> categories = Arrays.asList(objectMapper.readValue(validMessagesArray, Category[].class));

        // when
        when(categoryRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(categories));
        Page<CategoryDTO> result = categoryService.getAllCategories(pageable);

        // then
        assertAll("Category DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.getContent().size()),
                () -> {
                    CategoryDTO categoryDTO1 = result.getContent().get(0);
                    assertThat(categoryDTO1).isNotNull();
                    assertThat(categoryDTO1.getId()).isEqualTo(1L);
                    assertThat(categoryDTO1.getName()).isEqualTo("Name 1");
                    assertThat(categoryDTO1.getDescription()).isEqualTo("Description 1");
                },
                () -> {
                    CategoryDTO categoryDTO2 = result.getContent().get(1);
                    assertThat(categoryDTO2).isNotNull();
                    assertThat(categoryDTO2.getId()).isEqualTo(2L);
                    assertThat(categoryDTO2.getName()).isEqualTo("Name 2");
                    assertThat(categoryDTO2.getDescription()).isEqualTo("Description 2");
                }
        );
    }

    @Test
    void whenUserLoggedInThenSaveCategorySuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/category/Category.json");
        Category category = objectMapper.readValue(validMessage, Category.class);

        // when
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryDTO result = categoryService.saveCategory(categoryDTOMock);

        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("New Category", result.getName()),
                () -> assertEquals("Category Description", result.getDescription())
        );
    }

    @Test
    void whenUserLoggedInThenUpdateCategorySuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/category/CategoryUpdated.json");
        Category categoryUpdated = objectMapper.readValue(validMessage, Category.class);
        CategoryDTO categoryDTO = objectMapper.readValue(validMessage, CategoryDTO.class);

        // when
        when(categoryRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(categoryUpdated));
        when(categoryRepository.save(any(Category.class))).thenReturn(categoryUpdated);
        CategoryDTO updatedCategoryDTO = categoryService.updateCategory(1L, categoryDTO);

        // then
        assertNotNull(updatedCategoryDTO);
        assertEquals("Updated Category", updatedCategoryDTO.getName());
        assertEquals("Updated Description", updatedCategoryDTO.getDescription());
        verify(categoryRepository, times(1)).save(categoryUpdated);
    }

    @Test
    void whenUserLoggedInThenDeleteCategorySuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/category/CategoryUpdated.json");
        Category category = objectMapper.readValue(validMessage, Category.class);
        when(categoryRepository.findByUserIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(category));

        // when
        categoryService.deleteCategoryById(category.getId());

        // then
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void whenUserLoggedInThenDeleteAllCategoriesForUserSuccess() throws IOException {

        // given
        Pageable pageable = PageRequest.of(0, 2);
        String validMessagesArray = FileHelper.readFromFile("requests/category/CategoryArray.json");
        List<Category> categories = Arrays.asList(objectMapper.readValue(validMessagesArray, Category[].class));
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        // when
        when(categoryRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(categoryPage);
        categoryService.deleteAllCategoriesForUser(pageable);

        // then
        verify(categoryRepository, times(1)).deleteAll(categories);
        verify(categoryRepository, times(1)).findByUserId(anyLong(), any(Pageable.class));
    }

    @Test
    void whenCategoryDTOIsNullThenThrowIllegalArgumentException() {

        // when & then
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.saveCategory(null);
        });
        assertTrue(illegalArgumentException.getMessage().contains("CategoryDTO cannot be null"));
    }

    @Test
    void whenCategoryAlreadyExistsThenThrowItemAlreadyExistsException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/category/Category.json");
        CategoryDTO categoryDTO = objectMapper.readValue(validMessage, CategoryDTO.class);

        // when
        when(categoryRepository.existsByNameAndUserId(categoryDTO.getName(), 1L)).thenReturn(true);

        // then
        ItemAlreadyExistsException itemAlreadyExistsException = assertThrows(ItemAlreadyExistsException.class, () -> {
            categoryService.saveCategory(categoryDTO);
        });
        assertTrue(itemAlreadyExistsException.getMessage().contains("Category with name: New Category already exists"));
    }

    @Test
    void whenUserLoggedNotFoundThenThrowResourceNotFoundException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/category/CategoryUpdated.json");
        Category categoryUpdated = objectMapper.readValue(validMessage, Category.class);
        CategoryDTO categoryDTO = objectMapper.readValue(validMessage, CategoryDTO.class);

        // when
        when(categoryRepository.save(any(Category.class))).thenReturn(categoryUpdated);

        // then
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.updateCategory(categoryDTO.getId(), categoryDTO);
        });
        assertTrue(resourceNotFoundException.getMessage().contains("Category is not found for id"));
    }

    @Test
    void whenUserLoggedNotFoundForDeleteByIdThenThrowResourceNotFoundException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/category/Category.json");
        Category category = objectMapper.readValue(validMessage, Category.class);

        // then & then
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategoryById(category.getId());
        });
        assertTrue(resourceNotFoundException.getMessage().contains("Category is not found for id"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void whenInvalidPageableThenThrowIllegalArgumentException() {

        // when
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.deleteAllCategoriesForUser(null);
        });

        // then
        verify(categoryRepository, never()).findByUserId(anyLong(), any(Pageable.class));
    }

    @Test
    void whenNoCategoriesFoundThenThrowResourceNotFoundException() {

        // given
        Pageable pageable = PageRequest.of(0, 2);
        Page<Category> emptyPage = Page.empty(pageable);
        when(categoryRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(emptyPage);

        // when
        ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteAllCategoriesForUser(pageable);
        });

        // then
        assertTrue(resourceNotFoundException.getMessage().contains("No categories found for user"));
    }
}