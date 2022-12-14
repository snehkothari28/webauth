package com.sk.webauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebauthApplication {

    //    private HmacOneTimePasswordGenerator hotp;
//    private Key key;
    //	private String encodedKey ="MTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
    private int counter = 0;

    public static void main(String[] args) {
        SpringApplication.run(WebauthApplication.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//    }

//    public void setUp() throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException {
//
//        Scanner in = new Scanner(System.in);
//
//        String encodedKey = in.nextLine();
//        generateTOTP(encodedKey);
//		for (int i = 0; i < 5; i++) {
//			Thread.sleep(30*1000);
//			printCurrentTOTP();
//		}
//		generateHOTP(encodedKey);
//    }


//	private void generateHOTP(String encodedKey) {
//        this.hotp = new HmacOneTimePasswordGenerator();
//
////		final KeyGenerator keyGenerator = KeyGenerator.getInstance(this.hotp.getAlgorithm());
////		keyGenerator.init(512);
////		this.key = keyGenerator.generateKey();
//
//
//
//        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
//        this.key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//        System.out.println("Key is " + Base64.getEncoder().encodeToString(this.key.getEncoded()));
//    }
//
//    public int benchmarkGenerateOneTimePassword() throws InvalidKeyException {
//        return this.hotp.generateOneTimePassword(this.key, this.counter++);
//    }
//
//    public String benchmarkGenerateOneTimePasswordString() throws InvalidKeyException {
//        return this.hotp.generateOneTimePasswordString(this.key, this.counter++);
//    }
}
