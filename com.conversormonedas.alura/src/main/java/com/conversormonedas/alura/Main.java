package com.conversormonedas.alura;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String apiKey = System.getenv("EXCHANGE_RATE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR: No encuentro la variable de entorno EXCHANGE_RATE_API_KEY.");
            System.out.println("Configúrala en IntelliJ: Run -> Edit Configurations -> Environment variables.");
            return;
        }

        ExchangeRateClient client = new ExchangeRateClient(apiKey);

        while (true) {
            printMenu();
            int option = readInt("Elige una opción: ");

            if (option == 7) {
                System.out.println("¡Hasta luego!");
                break;
            }

            BigDecimal amount = readBigDecimal("Ingresa el valor que deseas convertir: ");

            try {
                ConversionRequest req = mapOption(option);
                if (req == null) {
                    System.out.println("Opción inválida. Intenta de nuevo.\n");
                    continue;
                }

                BigDecimal result = convert(client, req.from(), req.to(), amount);
                System.out.printf("El valor de %s %s corresponde al valor final de %s %s%n%n",
                        format(amount), req.from(), format(result), req.to());

            } catch (Exception e) {
                System.out.println("Ocurrió un error consultando la API: " + e.getMessage());
                System.out.println("Tip: revisa tu API Key y tu conexión a internet.\n");
            }
        }
    }

    private static void printMenu() {
        System.out.println("========================================");
        System.out.println("        CONVERSOR DE MONEDAS (JAVA)      ");
        System.out.println("========================================");
        System.out.println("1) USD -> ARS (Dólar a Peso Argentino)");
        System.out.println("2) ARS -> USD (Peso Argentino a Dólar)");
        System.out.println("3) USD -> BRL (Dólar a Real Brasileño)");
        System.out.println("4) BRL -> USD (Real Brasileño a Dólar)");
        System.out.println("5) USD -> COP (Dólar a Peso Colombiano)");
        System.out.println("6) COP -> USD (Peso Colombiano a Dólar)");
        System.out.println("7) Salir");
        System.out.println("----------------------------------------");
    }

    private static ConversionRequest mapOption(int option) {
        return switch (option) {
            case 1 -> new ConversionRequest("USD", "ARS");
            case 2 -> new ConversionRequest("ARS", "USD");
            case 3 -> new ConversionRequest("USD", "BRL");
            case 4 -> new ConversionRequest("BRL", "USD");
            case 5 -> new ConversionRequest("USD", "COP");
            case 6 -> new ConversionRequest("COP", "USD");
            default -> null;
        };
    }

    private static BigDecimal convert(ExchangeRateClient client, String from, String to, BigDecimal amount) throws Exception {
        Map<String, BigDecimal> rates = client.getRates(from);
        BigDecimal rate = rates.get(to);
        if (rate == null) {
            throw new IllegalStateException("No existe tasa para " + to + " desde base " + from);
        }
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número válido.");
            }
        }
    }

    private static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim().replace(",", ".");
            try {
                BigDecimal val = new BigDecimal(line);
                if (val.compareTo(BigDecimal.ZERO) < 0) {
                    System.out.println("Ingresa un valor >= 0.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número válido (ej: 25 o 25.50).");
            }
        }
    }

    private static String format(BigDecimal n) {
        return n.toPlainString();
    }

    private record ConversionRequest(String from, String to) {}
}