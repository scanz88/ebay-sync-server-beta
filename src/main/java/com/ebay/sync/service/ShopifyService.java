// ... existing code ...
public Product createProduct(ProductInput productData) {
    String mutation = """
        mutation productCreate($product: ProductInput!) {
          productCreate(product: $product) {
            product {
              id
              title
            }
            userErrors {
              field
              message
            }
          }
        }
    """;
    
    Map<String, Object> variables = new HashMap<>();
    variables.put("product", productData);
    
    return shopifyClient.executeGraphQL(mutation, variables);
}

public Product updateProduct(String productId, ProductInput productData) {
    String mutation = """
        mutation productUpdate($id: ID!, $product: ProductInput!) {
          productUpdate(id: $id, product: $product) {
            product {
              id
            }
            userErrors {
              field
              message
            }
          }
        }
    """;
    
    Map<String, Object> variables = new HashMap<>();
    variables.put("id", productId);
    variables.put("product", productData);
    
    return shopifyClient.executeGraphQL(mutation, variables);
}

public ProductVariant createVariant(String productId, VariantInput variantData) {
    String mutation = """
        mutation productVariantsBulkCreate($productId: ID!, $variants: [ProductVariantsBulkInput!]!) {
          productVariantsBulkCreate(productId: $productId, variants: $variants) {
            productVariants {
              id
              title
            }
            userErrors {
              field
              message
            }
          }
        }
    """;
    
    Map<String, Object> variables = new HashMap<>();
    variables.put("productId", productId);
    variables.put("variants", Collections.singletonList(variantData));
    
    return shopifyClient.executeGraphQL(mutation, variables);
}

public ProductVariant updateVariant(String variantId, VariantInput variantData) {
    String mutation = """
        mutation productVariantsBulkUpdate($variants: [ProductVariantsBulkInput!]!) {
          productVariantsBulkUpdate(variants: $variants) {
            productVariants {
              id
            }
            userErrors {
              field
              message
            }
          }
        }
    """;
    
    Map<String, Object> variables = new HashMap<>();
    VariantInput input = variantData;
    input.setId(variantId);
    variables.put("variants", Collections.singletonList(input));
    
    return shopifyClient.executeGraphQL(mutation, variables);
}

public void deleteVariant(String variantId) {
    String mutation = """
        mutation productVariantsBulkDelete($variantIds: [ID!]!) {
          productVariantsBulkDelete(variantIds: $variantIds) {
            userErrors {
              field
              message
            }
          }
        }
    """;
    
    Map<String, Object> variables = new HashMap<>();
    variables.put("variantIds", Collections.singletonList(variantId));
    
    shopifyClient.executeGraphQL(mutation, variables);
}