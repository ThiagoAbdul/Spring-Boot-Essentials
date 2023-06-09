package com.estudos.springframework.controller;

import com.estudos.springframework.dto.AnimePatchRequestBody;
import com.estudos.springframework.dto.AnimePostRequestBody;
import com.estudos.springframework.dto.AnimePutRequestBody;
import com.estudos.springframework.dto.AnimeResponse;
import com.estudos.springframework.exceptions.BadRequestException;
import com.estudos.springframework.exceptions.ResourceNotFoundException;
import com.estudos.springframework.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/animes")
@RequiredArgsConstructor
@Log4j2
public class AnimeController {

    private final AnimeService service;

    @GetMapping
    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @Operation(summary = "List all animes paginated", description = "Default size is 10")
    public ResponseEntity<CollectionModel<AnimeResponse>> listPageable(@ParameterObject Pageable page){
        Page<AnimeResponse> animeViews = service.listAll(page);
        return ResponseEntity.ok(hateoasOf(animeViews));
    }

    @GetMapping("/all")
    public ResponseEntity<CollectionModel<AnimeResponse>> listAll(){
        List<AnimeResponse> animeResponses = service.listAll();
        return ResponseEntity.ok(hateoasOf(animeResponses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeResponse> findById(@PathVariable long id){
        try{
            AnimeResponse animeResponse = service.findById(id);
            return ResponseEntity.ok(hateoasOf(animeResponse));
        }
        catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/find")
    public ResponseEntity<CollectionModel<AnimeResponse>> findAllByname(@RequestParam(required = false) String name){
        List<AnimeResponse> animeResponses = service.findAllByName(name);
        return ResponseEntity.ok(hateoasOf(animeResponses));
    }

    @PostMapping
    @CrossOrigin(origins = "http://127.0.0.1:5500")
    public ResponseEntity<AnimeResponse> save(@RequestBody @Valid AnimePostRequestBody anime){
        AnimeResponse savedAnime = service.save(anime);
        return new ResponseEntity<>(hateoasWith2Links(savedAnime), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful operation"),
            @ApiResponse(responseCode = "403", description = "If try to delete as USER authority"),
            @ApiResponse(responseCode = "400", description = "If anime id don't exists")
    })
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id) throws BadRequestException{
        service.delete(id);
        log.info("User {} deleted anime with id {}", userDetails.getUsername(), id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AnimeResponse> update(@PathVariable long id, @RequestBody AnimePatchRequestBody anime)
                                                                            throws BadRequestException{
        AnimeResponse updatedAnime = service.update(id, anime);
        return ResponseEntity.ok(hateoasWith2Links(updatedAnime));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimeResponse> replace(@PathVariable long id, @Valid @RequestBody AnimePutRequestBody anime)
                                                                                throws BadRequestException{
        AnimeResponse replacedAnime = service.replace(id, anime);
        return ResponseEntity.ok(hateoasWith2Links(replacedAnime));
    }

    private AnimeResponse hateoasOf(AnimeResponse animeResponse){
        animeResponse.add(linkTo(AnimeController.class).withSelfRel());
        return animeResponse;
    }

    private AnimeResponse hateoasWith2Links(AnimeResponse animeResponse){
        animeResponse.add(linkTo(
                methodOn(AnimeController.class).findById(animeResponse.getId())
        ).withSelfRel());
        return hateoasOf(animeResponse);
    }

    private CollectionModel<AnimeResponse> hateoasOf(Iterable<AnimeResponse> animeViews){
        for (var animeView: animeViews){
            animeView.add(linkTo(
                            methodOn(AnimeController.class).findById(animeView.getId())
                    ).withSelfRel()
            );
        }
        return CollectionModel.of(animeViews);
    }

}
