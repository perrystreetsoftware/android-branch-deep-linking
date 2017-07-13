package io.branch.branchandroiddemo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.PrefHelper;
import io.branch.referral.util.CommerceEvent;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.Product;
import io.branch.referral.util.ProductCategory;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)

public class ExampleUnitTest {

    @Mock
    Branch branch;

    @Mock
    PrefHelper prefHelper;

    @Mock
    BranchUniversalObject branchUniversalObject;

    @Mock
    Product product;

    @Test
    public void validateLogging() {
        Branch.enableLogging();
        assertThat(Branch.getIsLogging(), is(true));
    }

    @Test
    public void validateSimulatingInstalls() {
        Branch.enableSimulateInstalls();
        assertThat(Branch.isSimulatingInstalls(), is(true));
    }

//    @Test
//    public void validatePlayStoreReferralWaitTime() {
//        Branch.enablePlayStoreReferrer(1000L);
//        assertThat(Branch.getReferralFetchWaitTime(), is(1000L));
//    }

    @Test
    public void validateDisabledDeviceIDFetch() {
        Branch.disableDeviceIDFetch(true);
        assertThat(Branch.isDeviceIDFetchDisabled(), is(true));
    }

    @Test
    public void validateDisableEnableDeviceIDFetch() {
        Branch.disableDeviceIDFetch(true);
        assertThat(Branch.isDeviceIDFetchDisabled(), is(true));
        Branch.disableDeviceIDFetch(false);
        assertThat(Branch.isDeviceIDFetchDisabled(), is(false));
    }

    @Test
    public void validateBUOIsIndexable() {
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC);
        assertThat(branchUniversalObject.isPublicallyIndexable(), is(true));
    }

    @Test
    public void validateBUOIsNOTIndexable() {
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PRIVATE);
        assertThat(branchUniversalObject.isPublicallyIndexable(), is(false));
    }

    @Test
    public void validateBUOCanonicalIdentifier() {
        String STRING_TO_CONFIRM = "TEST_STRING";
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setCanonicalIdentifier(STRING_TO_CONFIRM);
        assertSame(branchUniversalObject.getCanonicalIdentifier(), STRING_TO_CONFIRM);
    }

    @Test
    public void validateBUOCanonicalUrl() {
        String STRING_TO_CONFIRM = "TEST_STRING";
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setCanonicalUrl(STRING_TO_CONFIRM);
        assertSame(branchUniversalObject.getCanonicalUrl(), STRING_TO_CONFIRM);
    }

    @Test
    public void validateBUOTitle() {
        String STRING_TO_CONFIRM = "TEST_STRING";
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setTitle(STRING_TO_CONFIRM);
        assertSame(branchUniversalObject.getTitle(), STRING_TO_CONFIRM);
    }

    @Test
    public void validateBUODescription() {
        String STRING_TO_CONFIRM = "TEST_STRING";
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setContentDescription(STRING_TO_CONFIRM);
        assertSame(branchUniversalObject.getDescription(), STRING_TO_CONFIRM);
    }

    @Test
    public void validateBUOImageUrl() {
        String STRING_TO_CONFIRM = "https://www.TEST_STRING.com/image.png";
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setContentImageUrl(STRING_TO_CONFIRM);
        assertSame(branchUniversalObject.getImageUrl(), STRING_TO_CONFIRM);
    }

    @Test
    public void validateBUOType() {
        String STRING_TO_CONFIRM = "MADE_UP_TYPE";
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setContentType(STRING_TO_CONFIRM);
        assertSame(branchUniversalObject.getType(), STRING_TO_CONFIRM);
    }

    @Test
    public void validateBUOPriceAndCurrency() {
        Double PRICE_TO_CONFIRM = 15d;
        CurrencyType CURRENCY_TYPE_TO_CONFIRM = CurrencyType.AED;
        branchUniversalObject = new BranchUniversalObject();
        branchUniversalObject.setPrice(PRICE_TO_CONFIRM, CURRENCY_TYPE_TO_CONFIRM);
        assertThat(branchUniversalObject.getPrice(), is(PRICE_TO_CONFIRM));
        assertThat(branchUniversalObject.getCurrencyType(), is(String.valueOf(CURRENCY_TYPE_TO_CONFIRM)));
    }

    @Test
    public void validateBUOExpirationTime() {
        branchUniversalObject = new BranchUniversalObject();
        Date date = new Date(System.currentTimeMillis());
        branchUniversalObject.setContentExpiration(date);
        assertThat(branchUniversalObject.getExpirationTime(), is(date.getTime()));
    }

    @Test
    public void validateCommerceRevenue() {
        Double revenueAmount = 50d;
        CommerceEvent commerceEvent = new CommerceEvent();
        commerceEvent.setRevenue(revenueAmount);
        assertThat(commerceEvent.getRevenue(), is(revenueAmount));
    }

    @Test
    public void validateCommerceCurrencyType() {
        CurrencyType currencyType = CurrencyType.ANG;
        CommerceEvent commerceEvent = new CommerceEvent();
        commerceEvent.setCurrencyType(currencyType);
        assertThat(commerceEvent.getCurrencyType(), is(currencyType));
    }

    @Test
    public void validateCommerceTransactionId() {
        String transactionID = "12345";
        CommerceEvent commerceEvent = new CommerceEvent();
        commerceEvent.setTransactionID(transactionID);
        assertThat(commerceEvent.getTransactionID(), is(transactionID));
    }

    @Test
    public void validateCommerceShipping() {
        Double shipping = 50d;
        CommerceEvent commerceEvent = new CommerceEvent();
        commerceEvent.setShipping(shipping);
        assertThat(commerceEvent.getShipping(), is(shipping));
    }

    @Test
    public void validateCommerceTax() {
        Double tax = 50d;
        CommerceEvent commerceEvent = new CommerceEvent();
        commerceEvent.setTax(tax);
        assertThat(commerceEvent.getTax(), is(tax));
    }

    @Test
    public void validateCommerceCoupon() {
        String coupon = "ohmycoupons";
        CommerceEvent commerceEvent = new CommerceEvent();
        commerceEvent.setCoupon(coupon);
        assertThat(commerceEvent.getCoupon(), is(coupon));
    }

    @Test
    public void validateCommerceAffiliation() {
        String affiliation = "affiliates";
        CommerceEvent commerceEvent = new CommerceEvent();
        commerceEvent.setAffiliation(affiliation);
        assertThat(commerceEvent.getAffiliation(), is(affiliation));
    }

    @Test
    public void validateProductName() {
        String productName = "decrypt0r";
        Product product = new Product();
        product.setName(productName);
        assertThat(product.getName(), is(productName));
    }

    @Test
    public void validateProductBrand() {
        String brandName = "TheBestOfTheBest";
        Product product = new Product();
        product.setBrand(brandName);
        assertThat(product.getBrand(), is(brandName));
    }

    @Test
    public void validateProductSKU() {
        String SKU = "what_is_SKU";
        Product product = new Product();
        product.setSku(SKU);
        assertThat(product.getSku(), is(SKU));
    }

    @Test
    public void validateProductCategory() {
        ProductCategory productCategory = ProductCategory.CAMERA_AND_OPTICS;
        Product product = new Product();
        product.setCategory(productCategory);
        assertThat(product.getCategory(), is(productCategory));
    }

    @Test
    public void validateProductQuantity() {
        int quantity = 100;
        Product product = new Product();
        product.setQuantity(quantity);
        assertThat(product.getQuantity(), is(quantity));
    }

    @Test
    public void validateProductPrice() {
        Double price = 50d;
        Product product = new Product();
        product.setPrice(price);
        assertThat(product.getPrice(), is(price));
    }

    @Test
    public void validateProductVariant() {
        String variant = "product_variant";
        Product product = new Product();
        product.setVariant(variant);
        assertThat(product.getVariant(), is(variant));
    }

    @Test
    public void validateCommerceEventProductSize() {
        int expectedSize = 3;
        CommerceEvent commerceEvent = new CommerceEvent();
        Product product1 = new Product();
        Product product2 = new Product();
        Product product3 = new Product();
        commerceEvent.addProduct(product1);
        commerceEvent.addProduct(product2);
        commerceEvent.addProduct(product3);
        assertThat(commerceEvent.getProducts().size(), is(expectedSize));
    }

    @Test
    public void validateCommerceEventProductSize2() {
        int expectedSize = 3;
        CommerceEvent commerceEvent = new CommerceEvent();
        Product product1 = new Product();
        Product product2 = new Product();
        Product product3 = new Product();
        List<Product> productList = new ArrayList<>();
        productList.add(product1);
        productList.add(product2);
        productList.add(product3);
        commerceEvent.setProducts(productList);
        assertThat(commerceEvent.getProducts().size(), is(expectedSize));
    }

    @Test
    public void validateProductJSONObjectName() {
        String expectedName = "EvanG";
        Product product = new Product();
        product.setName(expectedName);
        JSONObject jsonObject = product.getProductJSONObject();
        try {
            assertThat(jsonObject.getString("name"), is(expectedName));
        } catch ( JSONException e ) {
            assert false;
        }
    }

    @Test
    public void validateProductJSONObjectBrand() {
        String expectedBrand = "expectedBrand";
        Product product = new Product();
        product.setBrand(expectedBrand);
        JSONObject jsonObject = product.getProductJSONObject();
        try {
            assertThat(jsonObject.getString(expectedBrand), is(expectedBrand));
        } catch ( JSONException e ) {
            assert false;
        }
    }

    @Test
    public void validateProductJSONObjectVariant() {
        String expectedVariant = "expectedVariant";
        Product product = new Product();
        product.setVariant(expectedVariant);
        JSONObject jsonObject = product.getProductJSONObject();
        try {
            assertThat(jsonObject.getString(expectedVariant), is(expectedVariant));
        } catch ( JSONException e ) {
            assert false;
        }
    }

    @Test
    public void validateProductJSONObjectPrice() {
        Double expectedPrice = 15d;
        Product product = new Product();
        product.setPrice(expectedPrice);
        JSONObject jsonObject = product.getProductJSONObject();
        try {
            assertThat(jsonObject.getDouble("price"), is(expectedPrice));
        } catch ( JSONException e ) {
            assert false;
        }
    }

    @Test
    public void validateProductJSONObjectQuantity() {
        int expectedQuantity = 15;
        Product product = new Product();
        product.setQuantity(expectedQuantity);
        JSONObject jsonObject = product.getProductJSONObject();
        try {
            assertThat(jsonObject.getInt("quantity"), is(expectedQuantity));
        } catch ( JSONException e ) {
            assert false;
        }
    }

    @Test
    public void validateProductJSONObjectCategory() {
        ProductCategory expectedProductCategory = ProductCategory.TOYS_AND_GAMES;
        Product product = new Product();
        product.setCategory(expectedProductCategory);
        JSONObject jsonObject = product.getProductJSONObject();
        try {
            assertThat(jsonObject.get("category").toString(), is(expectedProductCategory.toString()));
        } catch ( JSONException e ) {
            assert false;
        }
    }

    @Test
    public void validateProductJSONObjectSKU() {
        String expectedSKU = "what_is_sku";
        Product product = new Product();
        product.setSku(expectedSKU);
        JSONObject jsonObject = product.getProductJSONObject();
        try {
            assertThat(jsonObject.getString("sku"), is(expectedSKU));
        } catch ( JSONException e ) {
            assert false;
        }
    }
}