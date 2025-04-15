package main

import (
	"context"
	"fmt"
	"log"
	"time"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetricgrpc"
	"go.opentelemetry.io/otel/metric"
	sdkmetric "go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"
	semconv "go.opentelemetry.io/otel/semconv/v1.4.0"
)

func main() {
	ctx := context.Background()

	// Create a resource describing this application
	res := resource.NewWithAttributes(
		semconv.SchemaURL,
		semconv.ServiceNameKey.String("otel-test-client"),
		semconv.ServiceVersionKey.String("0.1.0"),
	)

	// Configure the OTLP exporter
	exporter, err := otlpmetricgrpc.New(
		ctx,
		otlpmetricgrpc.WithInsecure(),
		otlpmetricgrpc.WithEndpoint("localhost:4317"),
	)
	if err != nil {
		log.Fatalf("Failed to create OTLP exporter: %v", err)
	}

	// Create a meter provider with the exporter
	meterProvider := sdkmetric.NewMeterProvider(
		sdkmetric.WithReader(sdkmetric.NewPeriodicReader(exporter)),
		sdkmetric.WithResource(res),
	)
	defer func() {
		if err := meterProvider.Shutdown(ctx); err != nil {
			log.Fatalf("Failed to shutdown meter provider: %v", err)
		}
	}()

	// Set the global meter provider
	otel.SetMeterProvider(meterProvider)

	// Get a meter
	meter := meterProvider.Meter("test-meter")

	// Create a counter
	counter, err := meter.Int64Counter(
		"test_counter",
		metric.WithDescription("Test counter"),
	)
	if err != nil {
		log.Fatalf("Failed to create counter: %v", err)
	}

	fmt.Println("Sending metrics to OpenTelemetry Collector...")

	// Add values to counter
	for i := 0; i < 10; i++ {
		counter.Add(ctx, 1)
		fmt.Printf("Added %d to counter\n", 1)
		time.Sleep(1 * time.Second)
	}

	fmt.Println("Metrics sent. Check the OpenTelemetry Collector logs.")
}
