package com.emrecelen.rate_hub.api.snapshot;

import com.emrecelen.rate_hub.model.RateView;
import com.emrecelen.rate_hub.service.RateStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rates")
public class RateSnapshotController {

    private final RateStoreService store;

    public RateSnapshotController(RateStoreService store) {
        this.store = store;
    }

    @GetMapping
    public ResponseEntity<List<RateView>> findAll() {
        return ResponseEntity.ok(store.getAll());
    }

    @GetMapping("/{base}/{quote}")
    public ResponseEntity<RateView> findByPair(@PathVariable("base") String base, @PathVariable(name = "quote") String quote) {
        RateView v = store.get(base.toUpperCase().concat("/").concat(quote.toUpperCase()));
        return v == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(v);
    }
}
