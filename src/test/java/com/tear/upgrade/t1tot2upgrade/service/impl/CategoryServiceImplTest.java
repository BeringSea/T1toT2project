package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    @WithMockUser
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
    @WithMockUser
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

    // TODO Just for orientation - Invalid test cases start form here

    @Test
    @WithMockUser
    void whenCategoryDTOIsNullThenThrowIllegalArgumentException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.saveCategory(null);
        }, "CategoryDTO cannot be null");
    }

    @Test
    @WithMockUser
    void whenCategoryAlreadyExistsThenThrowItemAlreadyExistsException() throws IOException {
        // given
        String validMessage = FileHelper.readFromFile("requests/category/Category.json");
        CategoryDTO categoryDTO = objectMapper.readValue(validMessage, CategoryDTO.class);

        // when
        when(categoryRepository.existsByNameAndUserId(categoryDTO.getName(), 1L)).thenReturn(true);

        // then
        assertThrows(ItemAlreadyExistsException.class, () -> {
            categoryService.saveCategory(categoryDTO);
        }, "Category with name: New Category already exists");
    }
}