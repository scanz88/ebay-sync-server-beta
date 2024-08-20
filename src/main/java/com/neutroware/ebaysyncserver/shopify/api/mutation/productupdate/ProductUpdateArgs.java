package com.neutroware.ebaysyncserver.shopify.api.mutation.productupdate;


import com.neutroware.ebaysyncserver.shopify.api.mutation.productcreate.ProductCreateArgs;

import java.util.List;

public record ProductUpdateArgs(
        ProductInput input,
        List<ProductCreateArgs.Media> media
) {
    public record ProductInput(
            String id,
            List<String> tags
    ) {}
}
