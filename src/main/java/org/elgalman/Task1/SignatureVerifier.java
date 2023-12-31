package org.elgalman.Task1;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class SignatureVerifier {
    private static final SecureRandom random = new SecureRandom();

    public static void main(String[] args) {
        // Генерация случайного простого числа p
        BigInteger p = generateRandomPrime(2048, 4096);
        System.out.println("p: " + p);

        // Выбор примитивного корня g
        BigInteger g = findPrimitiveRoot(p);
        System.out.println("g: " + g);

        // Генерация случайного секретного ключа a
        BigInteger a = generateRandomNumber(p.subtract(BigInteger.ONE));
        System.out.println("a: " + a);

        // Вычисление открытого ключа b = g^a mod p
        BigInteger b = g.modPow(a, p);
        System.out.println("b: " + b);

        // Сообщение для проверки подписи
        String message = "Hello, world!";
        System.out.println("Message: " + message);

        try {
            // Разбиение сообщения на блоки
            int blockSize = calculateBlockSize(p);
            String[] messageBlocks = splitMessage(message, blockSize);

            // Вычисление хэш-значения каждого блока сообщения H(m)
            BigInteger[] hValues = calculateHashes(messageBlocks);
            System.out.println("Hashes (H(m)): " + Arrays.toString(hValues));

            // Генерация случайного числа k
            BigInteger k;
            do {
                k = generateRandomNumber(p.subtract(BigInteger.ONE));
            } while (!k.gcd(p.subtract(BigInteger.ONE)).equals(BigInteger.ONE) || !b.gcd(p).equals(BigInteger.ONE));
            System.out.println("k: " + k);


            // Вычисление подписи для каждого блока
            BigInteger[] rValues = new BigInteger[messageBlocks.length];
            BigInteger[] sValues = new BigInteger[messageBlocks.length];

            for (int i = 0; i < messageBlocks.length; i++) {
                BigInteger h = hValues[i];

                // Вычисление первого компонента подписи r = g^k mod p
                BigInteger r = g.modPow(k, p);
                rValues[i] = r;
                System.out.println("r[" + i + "]: " + r);

                // Вычисление второго компонента подписи s = (H(m) - x*r) * k^(-1) mod (p-1)
                BigInteger kInverse = k.modInverse(p.subtract(BigInteger.ONE));
                BigInteger x = a.multiply(r).mod(p.subtract(BigInteger.ONE));
                BigInteger s = h.subtract(x).multiply(kInverse).mod(p.subtract(BigInteger.ONE));
                sValues[i] = s;
                System.out.println("s[" + i + "]: " + s);
            }

            // Перевірка підпису
            BigInteger y;
            try {
                y = b.modInverse(p); // Обернений елемент відкритого ключа
            } catch (ArithmeticException e) {
                System.out.println("Cannot calculate the inverse of b. Signature verification failed.");
                return;
            }

            if (!k.gcd(p.subtract(BigInteger.ONE)).equals(BigInteger.ONE) || !b.gcd(p).equals(BigInteger.ONE)) {
                System.out.println("k and p-1 are not coprime, or b and p are not coprime. Signature verification failed.");
                return;
            }



            BigInteger[] u1Values = new BigInteger[messageBlocks.length];
            BigInteger[] u2Values = new BigInteger[messageBlocks.length];

            for (int i = 0; i < messageBlocks.length; i++) {
                BigInteger h = hValues[i];
                BigInteger r = rValues[i];
                BigInteger s = sValues[i];

                BigInteger sInverse;
                try {
                    sInverse = s.modInverse(p.subtract(BigInteger.ONE));
                } catch (ArithmeticException e) {
                    System.out.println("Cannot calculate the inverse of s. Signature verification failed.");
                    return;
                }

                BigInteger u1 = h.multiply(sInverse).mod(p.subtract(BigInteger.ONE));

                // BigInteger u1 = h.multiply(s.modInverse(p.subtract(BigInteger.ONE))).mod(p.subtract(BigInteger.ONE));
                BigInteger u2 = r.multiply(s.modInverse(p.subtract(BigInteger.ONE))).mod(p.subtract(BigInteger.ONE));
                u1Values[i] = u1;
                u2Values[i] = u2;
                System.out.println("u1[" + i + "]: " + u1);
                System.out.println("u2[" + i + "]: " + u2);
            }

            BigInteger v = calculateV(g, y, u1Values, u2Values, p);
            System.out.println("v: " + v);

            // Подпись считается верной, если v = r для всех блоков
            boolean isValid = v.equals(rValues[0]); // Проверяем первое значение r
            for (int i = 1; i < rValues.length; i++) {
                isValid = isValid && v.equals(rValues[i]); // Проверяем остальные значения r
            }

            System.out.println("Signature is valid: " + isValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Генерация случайного простого числа длиной от minBits до maxBits битов
    private static BigInteger generateRandomPrime(int minBits, int maxBits) {
        BigInteger p;
        do {
            p = BigInteger.probablePrime(random.nextInt(maxBits - minBits + 1) + minBits, random);
        } while (!p.isProbablePrime(100));
        return p;
    }

    // Генерация случайного числа, взаимно простого с number
    private static BigInteger generateCoprime(BigInteger number) {
        BigInteger coprime;
        do {
            coprime = generateRandomNumber(number);
        } while (!coprime.gcd(number).equals(BigInteger.ONE));
        return coprime;
    }

    // Поиск примитивного корня g
    private static BigInteger findPrimitiveRoot(BigInteger p) {
        BigInteger phi = p.subtract(BigInteger.ONE); // phi(p) = p - 1
        BigInteger g;
        do {
            g = generateRandomNumber(p);
        } while (!isPrimitiveRoot(g, p, phi));
        return g;
    }

    // Генерация случайного числа в диапазоне от 1 до p-1
    private static BigInteger generateRandomNumber(BigInteger max) {
        BigInteger randomNumber;
        do {
            randomNumber = new BigInteger(max.bitLength(), random);
        } while (randomNumber.compareTo(BigInteger.ONE) < 0 || randomNumber.compareTo(max) > 0);
        return randomNumber;
    }

    // Проверка, является ли число g примитивным корнем модуля p
    private static boolean isPrimitiveRoot(BigInteger g, BigInteger p, BigInteger phi) {
        BigInteger exp = phi.divideAndRemainder(BigInteger.valueOf(2))[0]; // exp = (p-1)/2
        BigInteger result = g.modPow(exp, p);
        return !result.equals(BigInteger.ONE);
    }

    // Вычисление хэш-значения сообщения
    private static BigInteger calculateHash(String message) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageBytes = message.getBytes();
        byte[] digest = md.digest(messageBytes);
        return new BigInteger(1, digest);
    }

    // Вычисление размера блока сообщения в зависимости от модуля p
    private static int calculateBlockSize(BigInteger p) {
        int bitLength = p.bitLength() - 1;
        return (int) Math.ceil((double) bitLength / 8);
    }

    // Разбиение сообщения на блоки
    private static String[] splitMessage(String message, int blockSize) {
        int messageLength = message.length();
        int blockCount = (int) Math.ceil((double) messageLength / blockSize);
        String[] blocks = new String[blockCount];
        int startIndex = 0;

        for (int i = 0; i < blockCount; i++) {
            int endIndex = Math.min(startIndex + blockSize, messageLength);
            blocks[i] = message.substring(startIndex, endIndex);
            startIndex += blockSize;
        }

        return blocks;
    }

    // Вычисление хэш-значения каждого блока сообщения
    private static BigInteger[] calculateHashes(String[] blocks) throws NoSuchAlgorithmException {
        BigInteger[] hashes = new BigInteger[blocks.length];

        for (int i = 0; i < blocks.length; i++) {
            BigInteger hash = calculateHash(blocks[i]);
            hashes[i] = hash;
        }

        return hashes;
    }

    // Вычисление значения v
    private static BigInteger calculateV(BigInteger g, BigInteger y, BigInteger[] u1Values, BigInteger[] u2Values, BigInteger p) {
        BigInteger v = BigInteger.ONE;

        for (int i = 0; i < u1Values.length; i++) {
            BigInteger u1 = u1Values[i];
            BigInteger u2 = u2Values[i];

            BigInteger v1 = g.modPow(u1, p);
            BigInteger v2 = y.modPow(u2, p);
            BigInteger vPartial = v1.multiply(v2).mod(p);

            v = v.multiply(vPartial).mod(p);
        }

        return v;
    }
}
