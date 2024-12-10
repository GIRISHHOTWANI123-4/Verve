# Thought Process and Design Considerations

## Key Goals
1. Handle high-throughput traffic efficiently.
2. Ensure deduplication logic works in distributed environments.
3. Use logging and monitoring for better observability.
4. Leverage industry standards for distributed systems.

## Design Details
1. **Thread Safety**: Used `ConcurrentHashMap` for local deduplication. In a distributed setup, opted for Redis.
2. **Logging**: Log4j or SLF4J ensures structured logs for unique counts.
3. **HTTP Requests**: Used Java `HttpClient` for its asynchronous capabilities.
4. **Distributed Deduplication**: Chose Redis to centralize `id` storage for scalability.
5. **Streaming (Extension 3)**: Integrated Kafka to send unique counts to a topic for downstream processing.
