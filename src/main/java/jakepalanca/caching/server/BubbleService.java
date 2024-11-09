package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakepalanca.common.Bubble;
import jakepalanca.common.Coin;
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
                .region(Region.US_EAST_1) // Specify your AWS region
                .build();
        this.objectMapper = new ObjectMapper();

        // Set the naming strategy to SNAKE_CASE
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // Disable features as needed
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Optionally, include non-null fields
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.lambdaFunctionName = "bubbleService"; // Replace with your Lambda function's name
    }

    public List<Bubble> createBubbles(List<Coin> coins, String dataType, String timeInterval, int chartWidth, int chartHeight) {
        try {
            // Validate dataType and timeInterval
            if (!VALID_DATA_TYPES.contains(dataType)) {
                logger.error("Invalid dataType: {}", dataType);
                throw new IllegalArgumentException("Invalid dataType: " + dataType);
            }

            if (dataType.equals("price_change") && (timeInterval == null || !VALID_TIME_INTERVALS.contains(timeInterval))) {
                logger.error("Invalid timeInterval: {}", timeInterval);
                throw new IllegalArgumentException("Invalid timeInterval: " + timeInterval);
            }

            // Prepare the payload to send to the Lambda function
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("coins", coins);
            payloadMap.put("dataType", dataType);
            payloadMap.put("timeInterval", timeInterval);
            payloadMap.put("chartWidth", chartWidth);
            payloadMap.put("chartHeight", chartHeight);

            String payloadJson = objectMapper.writeValueAsString(payloadMap);
            SdkBytes payload = SdkBytes.fromUtf8String(payloadJson);

            logger.debug("Payload sent to Lambda: {}", payloadJson);

            // Create the invoke request
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(lambdaFunctionName)
                    .payload(payload)
                    .build();

            // Invoke the Lambda function
            InvokeResponse invokeResponse = awsLambda.invoke(invokeRequest);

            // Check for successful invocation
            if (invokeResponse.statusCode() != 200) {
                logger.error("Lambda invocation failed with status code: {}", invokeResponse.statusCode());
                throw new RuntimeException("Lambda invocation failed");
            }

            // Read the response
            String responseJson = invokeResponse.payload().asUtf8String();

            logger.debug("Response from Lambda: {}", responseJson);

            // Deserialize the response into a list of Bubble objects
            List<Bubble> bubbles = objectMapper.readValue(responseJson, new TypeReference<List<Bubble>>() {});

            logger.debug("Deserialized Bubbles: {}", objectMapper.writeValueAsString(bubbles));

            // Additional validation: Ensure bubbles are not null and contain valid data
            if (bubbles == null || bubbles.isEmpty()) {
                logger.warn("No bubbles received from Lambda.");
            }

            return bubbles;

        } catch (LambdaException e) {
            logger.error("Error invoking Lambda function: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create bubbles via Lambda function", e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }

}
