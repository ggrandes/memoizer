package org.javastack.memoizer.example;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

import org.javastack.memoizer.Memoizer;

public class Example {
	/**
	 * Sample Interface to Memoize
	 */
	public static interface SampleInterface {
		public String hash(final String in) throws NoSuchAlgorithmException;
	}

	/**
	 * Sample Slow Implementation (MessageDigest with SHA-512)
	 */
	public static class SampleSlowImpl implements SampleInterface {
		private static final Charset UTF8 = Charset.forName("UTF-8");

		public String hash(final String in) throws NoSuchAlgorithmException {
			final MessageDigest md = MessageDigest.getInstance("SHA-512");
			final byte[] buf = md.digest(in.getBytes(UTF8));
			return DatatypeConverter.printBase64Binary(buf);
		}
	}

	private static final String getHeader(final Class<?> b1, //
			final Class<?> b2) {
		final String s1 = b1.getSimpleName();
		final String s2 = b2.getSimpleName();
		if (s1.equals(s2))
			return s1 + ":direct";
		return s1 + ":memoize";
	}

	/**
	 * Simple Test / Benchmark
	 */
	public static void main(final String[] args) throws Throwable {
		final int TOTAL = (int) 1e6;
		final String TEST_TEXT = "hello world";
		final int cacheElements = 1024;
		final long cacheMillis = 1000; // 1 second
		final SampleInterface[] samples = new SampleInterface[] {
				new SampleSlowImpl(), //
				(SampleInterface) Memoizer.memoize(new SampleSlowImpl(), cacheElements, cacheMillis)
		};
		//
		long ts, diff;
		for (int k = 0; k < samples.length; k++) {
			final SampleInterface base = samples[k & ~1];
			final SampleInterface test = samples[k];
			final String hdr = getHeader(base.getClass(), test.getClass());
			ts = System.currentTimeMillis();
			for (int i = 0; i < TOTAL; i++) {
				test.hash(TEST_TEXT);
			}
			diff = System.currentTimeMillis() - ts;
			System.out.println(hdr + "\t" + "diff=" + diff + "ms" + "\t" + //
					test.hash(TEST_TEXT));
		}
	}
}
