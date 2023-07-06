package org.elgalman.Task2;

import java.math.BigInteger;
import java.security.SecureRandom;


public class EncryptionExample {
    private static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) {
        // Генерація випадкового простого числа p
        BigInteger p = generateRandomPrime(2048, 4096);
        System.out.println("p: " + p);

        // Вибір примітивного кореня g
        BigInteger g = findPrimitiveRoot(p);
        System.out.println("g: " + g);

        // Генерація випадкового особистого ключа a
        BigInteger a = generateRandomNumber(p.subtract(BigInteger.ONE));
        System.out.println("a: " + a);

        // Обчислення відкритого ключа b = g^a mod p
        BigInteger b = g.modPow(a, p);
        System.out.println("b: " + b);

        // Повідомлення для шифрування
        String message = "15917362406071648803286951307997244379404379136339734634402494889278874667332345678909876543212343456787654323459876543223456789098765432123456789098765432"; // Приклад повідомлення
        System.out.println("Original Message: " + message);

        // Розмір блоку
        int blockSize = 32; // Розмір блоку в бітах

        // Шифрування повідомлення
        StringBuilder encryptedMessage = new StringBuilder();
        for (int i = 0; i < message.length(); i += blockSize) {
            String block = message.substring(i, Math.min(i + blockSize, message.length()));
            BigInteger m = new BigInteger(block);
            BigInteger k = generateRandomNumber(p.subtract(BigInteger.ONE));
            BigInteger x = g.modPow(k, p);
            BigInteger y = (b.modPow(k, p).multiply(m)).mod(p);
            encryptedMessage.append(x).append(",").append(y).append(";");
        }
        System.out.println("Encrypted Message: " + encryptedMessage);

        // Розшифрування повідомлення
        StringBuilder decryptedMessage = new StringBuilder();
        String[] blocks = encryptedMessage.toString().split(";");
        for (String block : blocks) {
            String[] parts = block.split(",");
            BigInteger x = new BigInteger(parts[0]);
            BigInteger y = new BigInteger(parts[1]);
            BigInteger s = x.modPow(a, p);
            BigInteger inverseS = s.modInverse(p);
            BigInteger decryptedBlock = (y.multiply(inverseS)).mod(p);
            decryptedMessage.append(decryptedBlock);
        }
        System.out.println("Decrypted Message: " + decryptedMessage);
    }

    // Генерація випадкового простого числа довжиною від minBits до maxBits бітів
    private static BigInteger generateRandomPrime(int minBits, int maxBits) {
        BigInteger p;
        do {
            p = BigInteger.probablePrime(random.nextInt(maxBits - minBits + 1) + minBits, random);
        } while (!p.isProbablePrime(100));
        return p;
    }

    // Пошук примітивного кореня g
    private static BigInteger findPrimitiveRoot(BigInteger p) {
        BigInteger phi = p.subtract(BigInteger.ONE); // phi(p) = p - 1
        BigInteger g;
        do {
            g = generateRandomNumber(p);
        } while (!isPrimitiveRoot(g, p, phi));
        return g;
    }

    // Генерація випадкового числа в діапазоні від 1 до p-1
    private static BigInteger generateRandomNumber(BigInteger max) {
        BigInteger randomNumber;
        do {
            randomNumber = new BigInteger(max.bitLength(), random);
        } while (randomNumber.compareTo(BigInteger.ONE) < 0 || randomNumber.compareTo(max) > 0);
        return randomNumber;
    }

    // Перевірка, чи є число g примітивним коренем модуля p
    private static boolean isPrimitiveRoot(BigInteger g, BigInteger p, BigInteger phi) {
        BigInteger exp = phi.divideAndRemainder(BigInteger.valueOf(2))[0]; // exp = (p-1)/2
        BigInteger result = g.modPow(exp, p);
        return !result.equals(BigInteger.ONE);
    }
}
