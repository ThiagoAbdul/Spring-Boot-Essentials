package com.estudos.springframework.controller;

import com.estudos.springframework.exceptions.ResourceNotFoundException;
import com.estudos.springframework.request.AnimeView;
import com.estudos.springframework.service.AnimeService;
import com.estudos.springframework.util.AnimeViewCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.hateoas.CollectionModel;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@ExtendWith(SpringExtension.class)
class AnimeControllerTest {

    @InjectMocks
    private AnimeController animeController;
    @Mock
    private AnimeService animeServiceMock;

    @BeforeEach
    void setup(){
        PageImpl<AnimeView> animePage = new PageImpl<>(AnimeViewCreator.createListOfAnimeView());
        BDDMockito.when(animeServiceMock.listAll(ArgumentMatchers.any()))
                .thenReturn(animePage);
        BDDMockito.when(animeServiceMock.listAll())
                .thenReturn(AnimeViewCreator.createListOfAnimeView());
        try {
            BDDMockito.when(animeServiceMock.findById(ArgumentMatchers.anyLong()))
                    .thenReturn(AnimeViewCreator.createAnimeView());
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Tests if the method listAllPageable passing an pageable as argument return a Page of AnimeView")
    void listPageableTest(){
        CollectionModel<AnimeView> expectedAnimeViews = CollectionModel.of(
                AnimeViewCreator.createPageOfAnimeView());
        CollectionModel<AnimeView> resultAnimeViews = animeController.listPageable(null).getBody();
        assert resultAnimeViews != null;
        List<String> expectedAnimeNames = expectedAnimeViews.getContent()
                .stream().map(AnimeView::getName)
                .collect(Collectors.toList());
        List<String> resultdAnimeNames = resultAnimeViews.getContent()
                .stream().map(AnimeView::getName)
                .collect(Collectors.toList());

                Assertions.assertThat(resultdAnimeNames).isEqualTo(expectedAnimeNames);

    }

    @Test
    @DisplayName("Test if the method listAll return a list of AnimeView")
    void listAllNonPageableTest(){
        CollectionModel<AnimeView> expectedListOfAnime = CollectionModel.of(
                AnimeViewCreator.createListOfAnimeView());
        CollectionModel<AnimeView> resultListOfAnime = animeController.listAll().getBody();
        assert resultListOfAnime != null;
        Assertions.assertThat(new ArrayList<>(resultListOfAnime.getContent()))
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(new ArrayList<>(expectedListOfAnime.getContent()));
    }

    @Test
    @DisplayName("Test the method findById when successful")
    void findByIdTest(){
        AnimeView expectedAnimeView = AnimeViewCreator.createAnimeView();
        AnimeView resultAnimeView = animeController.findById(ArgumentMatchers.anyLong()).getBody();
        Assertions.assertThat(resultAnimeView)
                .isNotNull()
                .isEqualTo(expectedAnimeView);
    }

    @Test
    @DisplayName("Test the method findById when fail")
    void findByIdFailTest(){
        try {
            BDDMockito.when(animeServiceMock.findById(ArgumentMatchers.anyLong()))
                    .thenThrow(ResourceNotFoundException.class);
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> animeController.findById(ArgumentMatchers.anyLong()));
    }

}