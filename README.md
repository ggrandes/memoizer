# memoizer

Agnostic Cache with Dynamic Proxies and Reflection in Java. Open Source Java project under Apache License v2.0

Memoize a function makes it faster by caching the return values of the function; If you call the function again with the same arguments, memoize gives you the value out of the cache, instead of letting the function compute the value all over again.

### Current Stable Version is [1.0.0](https://search.maven.org/#search|ga|1|g%3Aorg.javastack%20a%3Amemoizer)

---

## DOC

#### Usage Example

```java
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

	public static void main(final String[] args) throws Throwable {
		final int TOTAL = (int) 1e6;
		final String TEST_TEXT = "hello world";
		//
		final SampleInterface slowCode = new SampleSlowImpl();
		final int cacheElement = 16;
		final long cacheMillis = 1000; // 1 second
		final SampleInterface fast = (SampleInterface) Memoizer.memoize(
				slowCode, 
				cacheElement, 
				cacheMillis);
		//
		final long ts = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			fast.hash(TEST_TEXT);
		}
		final long diff = System.currentTimeMillis() - ts;
		System.out.println("diff=" + diff + "ms" + "\t" + //
				fast.hash(TEST_TEXT));
	}
}
```

* Full examples in [Example package](https://github.com/ggrandes/memoizer/tree/master/src/main/java/org/javastack/memoizer/example/)

---

## MAVEN

Add the dependency to your pom.xml:

    <dependency>
        <groupId>org.javastack</groupId>
        <artifactId>memoizer</artifactId>
        <version>1.0.0</version>
    </dependency>

---
Inspired in [Perl Memoize](http://perldoc.perl.org/Memoize.html), this code is Java-minimalistic version.
