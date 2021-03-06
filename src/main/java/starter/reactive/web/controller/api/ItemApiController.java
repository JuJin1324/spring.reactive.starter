package starter.reactive.web.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import starter.reactive.domain.ecommerce.dto.ItemCreateDto;
import starter.reactive.domain.ecommerce.dto.ItemReadDto;
import starter.reactive.domain.ecommerce.dto.ItemUpdateDto;
import starter.reactive.domain.ecommerce.service.ItemService;

import java.net.URI;

/**
 * Created by Yoo Ju Jin(jujin1324@daum.net)
 * Created Date : 2022/07/11
 */

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemApiController {
    private static final String INVENTORY = "INVENTORY";

    private final ItemService itemService;

    @GetMapping("")
    public Flux<ItemReadDto> findAll() {
        return itemService.getAllItems();
    }

    @GetMapping("/{itemId}")
    public Mono<ItemReadDto> findOne(@PathVariable("itemId") String itemId,
                                     Authentication authentication) {
        return itemService.getOne(itemId);
    }

    /* http POST http://localhost:8080/api/items name="name1" description="desc1" price=11.11 */

    @PreAuthorize("hasRole('" + INVENTORY + "')")
    @PostMapping("")
    public Mono<ResponseEntity<ItemReadDto>> addNewItem(@RequestBody Mono<ItemCreateDto> createDto, Authentication authentication) {
        return itemService.save(createDto)
                .map(ItemReadDto::getItemId)
                .flatMap(id -> findOne(id, authentication))
                .map(readDto -> ResponseEntity.created(URI.create("/api/items/" + readDto.getItemId()))
                        .body(readDto));
    }

    /*
     * http PUT http://localhost:8080/api/items/62cc266e7155b20d6853745c name="updated name" description="updated desc" price=22.22
     * http GET http://localhost:8080/api/items/62cc266e7155b20d6853745c
     */
    @PutMapping("/{itemId}")
    public Mono<ResponseEntity<ItemReadDto>> updateItem(@PathVariable("itemId") String itemId,
                                                        @RequestBody Mono<ItemUpdateDto> updateDto) {
        return itemService.update(itemId, updateDto)
                .map(readDto -> ResponseEntity.ok().body(readDto));
    }

    @PreAuthorize("hasRole('" + INVENTORY + "')")
    @DeleteMapping("/{itemId}")
    Mono<ResponseEntity<Void>> deleteItem(@PathVariable("itemId") String itemId) {
        return itemService.delete(itemId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
