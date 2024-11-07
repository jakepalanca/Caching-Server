package jakepalanca.caching.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BubbleService {

    private static final Logger logger = LoggerFactory.getLogger(BubbleService.class);
    private final LambdaClient awsLambda;
    private final ObjectMapper objectMapper;
    private final String lambdaFunctionName;

    // Define valid data types for bubble creation
    public static final Set<String> VALID_DATA_TYPES = Set.of(
            "market_cap",
            "total_volume",
            "price_change",
            "rank",
            "market_cap_change_percentage_24hr",
            "total_supply"
    );

    // Define valid time intervals for price change data type
    public static final Set<String> VALID_TIME_INTERVALS = Set.of(
            "1h",
            "24h",
            "7d",
            "14d",
            "30d",
            "200d",
            "1y"
    );

    public BubbleService() {
        // Initialize the AWS Lambda client
        this.awsLambda = LambdaClient.builder()
                .region(Region.US_WEST_2) // Specify your AWS region
                .build();
        this.objectMapper = new ObjectMapper();
        this.lambdaFunctionName = "BubblePackingFunction"; // Replace with your Lambda function's name
    }

    /**
     * Creates a list of {@code Bubble} objects from the given list of {@code Coin} objects.
     * This method sends the data to an AWS Lambda function for processing.
     *
     * @param coins        the list of {@code Coin} objects to be converted into bubbles
     * @param dataType     the data type for calculating bubble size
     * @param timeInterval the time interval for data types that require it
     * @param chartWidth   the width of the screen for positioning bubbles
     * @param chartHeight  the height of the screen for positioning bubbles
     * @return             a list of {@code Bubble} objects
     */
    public List<Bubble> createBubbles(List<Coin> coins, String dataType, String timeInterval, int chartWidth, int chartHeight) {
        try {
            // Prepare the payload to send to the Lambda function
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("coins", coins);
            payloadMap.put("dataType", dataType);
            payloadMap.put("timeInterval", timeInterval);
            payloadMap.put("chartWidth", chartWidth);
            payloadMap.put("chartHeight", chartHeight);

            String payloadJson = objectMapper.writeValueAsString(payloadMap);
            SdkBytes payload = SdkBytes.fromUtf8String(payloadJson);

            // Create the invoke request
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(lambdaFunctionName)
                    .payload(payload)
                    .build();

            // Invoke the Lambda function
            InvokeResponse invokeResponse = awsLambda.invoke(invokeRequest);

            // Read the response
            String responseJson = invokeResponse.payload().asUtf8String();

            // Deserialize the response into a list of Bubble objects
            return objectMapper.readValue(responseJson, new TypeReference<List<Bubble>>() {});

        } catch (LambdaException e) {
            logger.error("Error invoking Lambda function: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create bubbles via Lambda function", e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }
}
