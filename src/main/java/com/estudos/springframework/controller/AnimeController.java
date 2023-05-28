package com.estudos.springframework.controller;

import com.estudos.springframework.domain.Anime;
import com.estudos.springframework.exceptions.BadRequestException;
import com.estudos.springframework.exceptions.ResourceNotFoundException;
import com.estudos.springframework.request.AnimePatchRequestBody;
import com.estudos.springframework.request.AnimePostRequestBody;
import com.estudos.springframework.request.AnimePutRequestBody;
import com.estudos.springframework.request.AnimeView;
import com.estudos.springframework.service.AnimeService;
import com.estudos.springframework.util.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/animes")
@RequiredArgsConstructor
@Log4j2
public class AnimeController {

    private final DateUtil dateUtil;
    private final AnimeService service;

    @GetMapping({"/", ""})
    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @Operation(summary = "List all animes paginated", description = "Default size is 10")
    public ResponseEntity<Page<AnimeView>> listPageable(@ParameterObject Pageable page){
        //log.info(dateUtil.formatLocalDateTimeToSQLDate(LocalDateTime.now()));
        return ResponseEntity.ok(service.listAll(page));
    }

    @GetMapping("/public")
    public ResponseEntity<String> home(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok("Animes API");
    }

    @GetMapping("/all")
    public ResponseEntity<List<AnimeView>> listAll(){
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeView> findById(@PathVariable long id){
        try{
            return ResponseEntity.ok(service.findById(id));
        }
        catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/find")
    public ResponseEntity<List<AnimeView>> findAllByname(@RequestParam(required = false) String name){
        return ResponseEntity.ok(service.findAllByName(name));
    }

    @PostMapping
    @CrossOrigin(origins = "http://127.0.0.1:5500")
    public ResponseEntity<AnimeView> save(@RequestBody @Valid AnimePostRequestBody anime){
        AnimeView animeSaved = service.save(anime);
        return new ResponseEntity<>(animeSaved, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful operation"),
            @ApiResponse(responseCode = "403", description = "If try to delete as USER authority"),
            @ApiResponse(responseCode = "400", description = "If anime id don't exists")
    })
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable long id) throws BadRequestException{
        service.delete(id);
        log.info("User {} deleted anime with id {}", userDetails.getUsername(), id);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Anime> update(@PathVariable long id, @RequestBody AnimePatchRequestBody anime)
                                                                            throws BadRequestException{
        return ResponseEntity.ok(service.update(id, anime));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimeView> replace(@PathVariable long id, @Valid @RequestBody AnimePutRequestBody anime)
                                                                                throws BadRequestException{
        return ResponseEntity.ok(service.replace(id, anime));
    }
}
