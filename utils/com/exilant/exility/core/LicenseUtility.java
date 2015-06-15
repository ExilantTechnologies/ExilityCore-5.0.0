/* *******************************************************************************************************
Copyright (c) 2015 EXILANT Technologies Private Limited

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ******************************************************************************************************** */
package com.exilant.exility.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 
 */
@SuppressWarnings("restriction")
public class LicenseUtility {

	String licensePath = "exilant.lic";
	boolean isServerLicense = false;
	boolean isPageGeneratorLicense = false;
	int useCountLeft = 0;
	String expiresOn = "";

	/**
	 * 
	 * @param b
	 * @return string for a hex array
	 * @throws Exception
	 */
	public String getHexString(byte[] b) throws Exception {
		String result = "";
		for (byte element : b) {
			result += Integer.toString((element & 0xff) + 0x100, 16).substring(
					1);
		}
		return result;
	}

	/**
	 * 
	 * @param startDate
	 * @param endDate
	 * @param useCount
	 * @param hostnameText
	 * @param domainText
	 * @param macaddressText
	 * @throws Exception
	 */
	public void createLicense(Date startDate, Date endDate, int useCount,
			String hostnameText, String domainText, String macaddressText)
			throws Exception {
		String dateFormat = "M/d/yyyy H:m:s a";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		String computerName = "";
		String macAddress = "";

		if (hostnameText == "" || domainText == "") {
			computerName = InetAddress.getLocalHost().getCanonicalHostName()
					.replaceFirst("\\.", "");
		} else {
			computerName = hostnameText + domainText;
		}

		if (macaddressText == "") {
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				if (netint.getName().startsWith("eth")
						&& netint.getDisplayName().contains("Ethernet")) {
					if (netint.getHardwareAddress() != null) {
						macAddress = this.getHexString(
								netint.getHardwareAddress()).toUpperCase();
					}
				}
			}
		} else {
			macAddress = macaddressText;
		}

		String password = computerName + macAddress;
		String initVector = macAddress + computerName;
		char fieldSeparator = (char) 13;

		String dataToWrite = "" + sdf.format(startDate) + fieldSeparator
				+ sdf.format(endDate) + fieldSeparator + useCount;
		byte[] plainText = dataToWrite.getBytes();

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		MessageDigest algorithm = MessageDigest.getInstance("MD5");
		algorithm.reset();
		algorithm.update(password.getBytes());
		SecretKeySpec keySpec = new SecretKeySpec(algorithm.digest(), "AES");
		algorithm.reset();
		algorithm.update(initVector.getBytes());
		IvParameterSpec ivSpec = new IvParameterSpec(algorithm.digest());
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		byte[] results = cipher.doFinal(plainText);
		BASE64Encoder encoder = new BASE64Encoder();
		String licenseData = encoder.encode(results);

		BufferedWriter licenseWriter = new BufferedWriter(new FileWriter(
				"exilant.lic"));
		licenseWriter.write(licenseData);
		licenseWriter.close();
	}

	/**
	 * 
	 * @param isServer
	 * @return true if license is valid, false otherwise
	 * @throws Exception
	 */
	public boolean isValidLicense(boolean isServer) throws Exception {
		if (isServer) {
			this.isServerLicense = true;
		} else {
			this.isPageGeneratorLicense = true;
		}

		String dateFormat = "M/d/yyyy H:m:s a";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		String computerName = InetAddress.getLocalHost().getCanonicalHostName()
				.replaceFirst("\\.", "");

		String macAddress = "";

		Enumeration<NetworkInterface> nets = NetworkInterface
				.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if (netint.getName().startsWith("eth")
					&& netint.getDisplayName().contains("Ethernet")) {
				if (netint.getHardwareAddress() != null) {
					macAddress = this.getHexString(netint.getHardwareAddress())
							.toUpperCase();
				}
			}
		}

		String password = computerName + macAddress;
		String initVector = macAddress + computerName;
		char fieldSeparator = (char) 13;

		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(
				new FileReader("exilant.lic"));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();

		String readlicenseData = fileData.toString();
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] forDec = decoder.decodeBuffer(readlicenseData);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		MessageDigest algorithm = MessageDigest.getInstance("MD5");
		algorithm.reset();
		algorithm.update(password.getBytes());
		SecretKeySpec keySpec = new SecretKeySpec(algorithm.digest(), "AES");
		algorithm.reset();
		algorithm.update(initVector.getBytes());
		IvParameterSpec ivSpec = new IvParameterSpec(algorithm.digest());
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] results = cipher.doFinal(forDec);
		String decryptedData = new String(results);
		boolean isValidLicense = false;

		String[] licenseDetails = decryptedData.split(fieldSeparator + "");
		Calendar currentCal = Calendar.getInstance();
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(sdf.parse(licenseDetails[0]));
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(sdf.parse(licenseDetails[1]));
		int newUseCount = Integer.parseInt(licenseDetails[2]);

		if (startCal.before(currentCal) && currentCal.before(endCal)
				&& (newUseCount > 0)) {
			isValidLicense = true;
			this.expiresOn = licenseDetails[1];
			if (this.isPageGeneratorLicense && (!this.isServerLicense)) {
				newUseCount--;

				String updatedLicenseData = licenseDetails[0] + fieldSeparator
						+ licenseDetails[1] + fieldSeparator + newUseCount;

				byte[] plainText = updatedLicenseData.getBytes();
				Cipher newCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				MessageDigest newAlgorithm = MessageDigest.getInstance("MD5");
				newAlgorithm.reset();
				newAlgorithm.update(password.getBytes());
				SecretKeySpec newKeySpec = new SecretKeySpec(
						newAlgorithm.digest(), "AES");
				newAlgorithm.reset();
				newAlgorithm.update(initVector.getBytes());
				IvParameterSpec newIvSpec = new IvParameterSpec(
						newAlgorithm.digest());
				newCipher.init(Cipher.ENCRYPT_MODE, newKeySpec, newIvSpec);
				byte[] newResults = newCipher.doFinal(plainText);
				BASE64Encoder newEncoder = new BASE64Encoder();
				String newLicenseData = newEncoder.encode(newResults);

				BufferedWriter licenseWriter = new BufferedWriter(
						new FileWriter("exilant.lic"));
				licenseWriter.write(newLicenseData);
				licenseWriter.close();
			}
		}

		return isValidLicense;
	}

	/**
	 * 
	 * @return number of uses left for this license
	 */
	public int getUseCountLeft() {
		return this.useCountLeft;
	}

	/**
	 * 
	 * @return days left for this license
	 * @throws Exception
	 */
	public long getDaysLeft() throws Exception {
		String dateFormat = "M/d/yyyy H:m:s a";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date expireDate = sdf.parse(this.expiresOn);
		Date currentDate = new Date();
		return (expireDate.getTime() - currentDate.getTime()) / 1000 * 60 * 60
				* 24;
	}
}
