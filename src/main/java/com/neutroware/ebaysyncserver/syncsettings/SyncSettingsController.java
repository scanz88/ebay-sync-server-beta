package com.neutroware.ebaysyncserver.syncsettings;

import com.neutroware.ebaysyncserver.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(
        origins = "*"
)
@RequestMapping("syncsettings")
@RequiredArgsConstructor
public class SyncSettingsController {

    private final SyncSettingsService syncSettingsService;

    @GetMapping
    public SyncSettingsResponse get(Authentication currentUser) {
        return syncSettingsService.findByUserId(currentUser.getName());
    }

    @GetMapping
    @RequestMapping("{id}")
    public SyncSettingsResponse get(@PathVariable Long id) {
        return syncSettingsService.findById(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody final SyncSettingsRequest request, Authentication currentUser) {
        return syncSettingsService.create(request, currentUser);
    }

    @PutMapping
    public Long update(@RequestBody final SyncSettingsRequest request, Authentication currentUser) {
        return syncSettingsService.update(request, currentUser.getName());
    }
}
