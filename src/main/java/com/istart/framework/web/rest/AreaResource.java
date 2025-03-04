package com.istart.framework.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.istart.framework.domain.Area;
import com.istart.framework.service.AreaService;
import com.istart.framework.web.rest.util.HeaderUtil;
import com.istart.framework.web.rest.util.PaginationUtil;
import com.istart.framework.web.rest.dto.AreaDTO;
import com.istart.framework.web.rest.mapper.AreaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Area.
 */
@RestController
@RequestMapping("/api")
public class AreaResource {

    private final Logger log = LoggerFactory.getLogger(AreaResource.class);
        
    @Inject
    private AreaService areaService;
    
    @Inject
    private AreaMapper areaMapper;
    
    /**
     * POST  /areas : Create a new area.
     *
     * @param areaDTO the areaDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new areaDTO, or with status 400 (Bad Request) if the area has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/areas",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<AreaDTO> createArea(@RequestBody AreaDTO areaDTO) throws URISyntaxException {
        log.debug("REST request to save Area : {}", areaDTO);
        if (areaDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("area", "idexists", "A new area cannot already have an ID")).body(null);
        }
        AreaDTO result = areaService.save(areaDTO);
        return ResponseEntity.created(new URI("/api/areas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("area", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /areas : Updates an existing area.
     *
     * @param areaDTO the areaDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated areaDTO,
     * or with status 400 (Bad Request) if the areaDTO is not valid,
     * or with status 500 (Internal Server Error) if the areaDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/areas",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<AreaDTO> updateArea(@RequestBody AreaDTO areaDTO) throws URISyntaxException {
        log.debug("REST request to update Area : {}", areaDTO);
        if (areaDTO.getId() == null) {
            return createArea(areaDTO);
        }
        AreaDTO result = areaService.save(areaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("area", areaDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /areas : get all the areas.
     *  http://localhost:8080/api/areas?cacheBuster=1464244135970&page=0&size=20&sort=id,asc
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of areas in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/areas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<AreaDTO>> getAllAreas(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Areas");
        Page<Area> page = areaService.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/areas");
        return new ResponseEntity<>(areaMapper.areasToAreaDTOs(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /areas/:id : get the "id" area.
     *
     * @param id the id of the areaDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the areaDTO, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/areas/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<AreaDTO> getArea(@PathVariable Long id) {
        log.debug("REST request to get Area : {}", id);
        AreaDTO areaDTO = areaService.findOne(id);
        return Optional.ofNullable(areaDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /areas/:id : delete the "id" area.
     *
     * @param id the id of the areaDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/areas/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteArea(@PathVariable Long id) {
        log.debug("REST request to delete Area : {}", id);
        areaService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("area", id.toString())).build();
    }

    /**
     * SEARCH  /_search/areas?query=:query : search for the area corresponding
     * to the query.
     *
     * @param query the query of the area search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/areas",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<AreaDTO>> searchAreas(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Areas for query {}", query);
        Page<Area> page = areaService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/areas");
        return new ResponseEntity<>(areaMapper.areasToAreaDTOs(page.getContent()), headers, HttpStatus.OK);
    }

}
