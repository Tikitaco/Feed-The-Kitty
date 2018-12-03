package edu.umd.cs.fatcat.dwolla;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.CustomersApi;
import io.swagger.client.api.FundingsourcesApi;
import io.swagger.client.api.TransfersApi;
import io.swagger.client.model.Amount;
import io.swagger.client.model.CreateCustomer;
import io.swagger.client.model.CreateFundingSourceRequest;
import io.swagger.client.model.FundingSource;
import io.swagger.client.model.HalLink;
import io.swagger.client.model.MicroDeposits;
import io.swagger.client.model.Transfer;
import io.swagger.client.model.TransferRequestBody;
import io.swagger.client.model.Unit$;
import io.swagger.client.model.VerifyMicroDepositsRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DwollaUtil {

    // Return String of newly create customer ID on success or null on failure
    public String createCustomer(String email, String firstName, String lastName, String address1, String address2,
                                 String city, String state, String postalCode, String dateOfBirth, String ssn) throws IOException {

        CustomersApi customersApi = new CustomersApi(getApiClient());

        CreateCustomer cust = new CreateCustomer();
        cust.setEmail(email);
        cust.setFirstName(firstName);
        cust.setLastName(lastName);
        cust.setType("personal");
        cust.setAddress1(address1);

        if (address2 != null && !address2.isEmpty()) {
            cust.setAddress2(address2); // Optional
        }

        cust.setCity(city);
        cust.setState(state); // Must be 2 letter abbreviation
        cust.setPostalCode(postalCode);
        cust.setDateOfBirth(dateOfBirth); // Must be in yyyy-MM-dd
        cust.setSsn(ssn); // Last four digits

        Unit$ result;
        try {
            result = customersApi.create(cust);

            Pattern pattern = Pattern.compile(".*customers/(.*)");
            Matcher matcher = pattern.matcher(result.getLocationHeader());
            matcher.matches();

            return matcher.group(1);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String createFundingSource(String customerId, String routingNumber, String accountNumber,
                                      String type, String name) throws IOException {

        FundingsourcesApi fundingApi = new FundingsourcesApi(getApiClient());

        CreateFundingSourceRequest fund = new CreateFundingSourceRequest();
        fund.setRoutingNumber(routingNumber);
        fund.setAccountNumber(accountNumber);
        fund.setType(type);
        fund.setName(name);

        FundingSource result;
        try {
            result = fundingApi.createCustomerFundingSource(fund, customerId);

            Pattern pattern = Pattern.compile(".*funding-sources/(.*)");
            Matcher matcher = pattern.matcher(result.getLocationHeader());
            matcher.matches();

            return matcher.group(1);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return null;

    }

    public String createTransfer(String fundingSource, String customerDest, String sAmount) throws IOException {

        Map<String, HalLink> links = new HashMap<String, HalLink>();

        HalLink source = new HalLink();
        source.setHref("https://api-sandbox.dwolla.com/funding-sources/" + fundingSource);
        links.put("source", source);

        HalLink destination = new HalLink();
        destination.setHref("https://api-sandbox.dwolla.com/customers/" + customerDest);
        links.put("destination", destination);

        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setValue(sAmount);

        TransfersApi transfersApi = new TransfersApi(getApiClient());

        TransferRequestBody transfer = new TransferRequestBody();
        transfer.setLinks(links);
        transfer.setAmount(amount);

        Unit$ result;
        try {
            result = transfersApi.create(transfer);

            Pattern pattern = Pattern.compile(".*transfers/(.*)$");
            Matcher matcher = pattern.matcher(result.getLocationHeader());
            matcher.matches();

            return matcher.group(1);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return null;

    }

    public String getTransferStatus(String transferId) throws IOException {

        TransfersApi transfersApi = new TransfersApi(getApiClient());

        Transfer transfer;
        try {
            transfer = transfersApi.byId(transferId);

            return transfer.getStatus();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return null;

    }

    public boolean initiateMicroDeposit(String fundId) throws IOException {

        FundingsourcesApi fundingApi = new FundingsourcesApi(getApiClient());

        MicroDeposits result;
        try {
            result = fundingApi.microDeposits(null, fundId);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean verifyMicroDeposit(String fundId, String sAmount1, String sAmount2) throws IOException {

        Amount amount1 = new Amount();
        amount1.setCurrency("USD");
        amount1.setValue(sAmount1);

        Amount amount2 = new Amount();
        amount2.setCurrency("USD");
        amount2.setValue(sAmount2);

        FundingsourcesApi fundingApi = new FundingsourcesApi(getApiClient());

        VerifyMicroDepositsRequest microDepositsRequest = new VerifyMicroDepositsRequest();
        microDepositsRequest.setAmount1(amount1);
        microDepositsRequest.setAmount2(amount2);

        MicroDeposits result;
        try {
            result =  fundingApi.microDeposits(microDepositsRequest, fundId);
            return true;
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return false;

    }

    public ApiClient getApiClient() throws IOException {

        ApiClient a = new ApiClient();
        a.setBasePath("https://api-sandbox.dwolla.com");

        String token = getDwollaToken();
        a.setAccessToken(token);

        return a;

    }

    // No method available in swagger generated java client, need to make a manual POST to Dwolla for access token
    public String getDwollaToken() throws IOException {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials");
        Request request = new Request.Builder()
                .url("https://accounts-sandbox.dwolla.com/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic RGV2SEREWU9DSmp5UzB6M1NnQ253RlZYbFBzZ0pseHFXbFNuS2wwcWRrTW5meFJQazk6dVhxVmpySlJFeU0zQmU2QW9OTnJRZWlIdWFVamEwbUxTeE5FYWJvYmtMZUFrcE9hRGU=")
                .addHeader("cache-control", "no-cache")
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() == 200) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> map = gson.fromJson(response.body().string(), type);
            return map.get("access_token");
        }

        return null;

    }
}
