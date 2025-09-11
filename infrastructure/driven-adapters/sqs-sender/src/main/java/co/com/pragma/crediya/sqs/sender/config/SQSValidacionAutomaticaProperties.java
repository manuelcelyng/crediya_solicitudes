package co.com.pragma.crediya.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades específicas para el publisher de Validación Automática.
 *
 * Nota de diseño (actual):
 * - Este módulo reutiliza un único SqsAsyncClient definido en {@link SQSSenderConfig},
 *   el cual se construye con {@link SQSSenderProperties} (region/endpoint/credenciales).
 * - Por lo tanto, en esta clase actualmente SOLO se usa "queueUrl".
 * - Los campos "region" y "endpoint" quedan reservados para un escenario futuro en el que se
 *   necesite un cliente SQS distinto específicamente para Validación Automática. Hoy no están
 *   cableados a ningún bean de cliente.
 */
@ConfigurationProperties(prefix = "adapter.sqs.validacion-automatica")
public record SQSValidacionAutomaticaProperties(
        String region,
        String queueUrl,
        String endpoint
) {
}
