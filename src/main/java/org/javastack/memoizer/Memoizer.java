package org.javastack.memoizer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agnostic Cache for Method Invokation using Reflection
 */
public class Memoizer implements InvocationHandler {
	/**
	 * Default: 1024 elements
	 */
	public static final int DEFAULT_CACHE_MAX_ELEMENTS = 1024;
	/**
	 * Default: 1000millis
	 */
	public static final long DEFAULT_CACHE_EXPIRE_MILLIS = 1000L; // 1 second

	private final Object object;
	private final Map<CacheKey, CacheValue> cache;
	private final long expireMillis;

	/**
	 * Memoize object using default maxElement and default expireMillis
	 * 
	 * @param origin object to speedup
	 * @return proxied object
	 * @see #memoize(Object, int, long)
	 */
	public static Object memoize(final Object origin) //
			throws InstantiationException, IllegalAccessException {
		return memoize(origin, DEFAULT_CACHE_MAX_ELEMENTS, DEFAULT_CACHE_EXPIRE_MILLIS);
	}

	/**
	 * Memoize object
	 * 
	 * @param origin object to speedup
	 * @param maxElements limit elements to cache
	 * @param expireMillis expiration time in millis
	 * @return proxied object
	 */
	public static Object memoize(final Object origin, //
			final int maxElements, final long expireMillis) //
			throws InstantiationException, IllegalAccessException {
		final Class<?> clazz = origin.getClass();
		final Memoizer memoizer = new Memoizer(origin, maxElements, expireMillis);
		return Proxy.newProxyInstance(clazz.getClassLoader(), //
				clazz.getInterfaces(), memoizer);
	}

	private Memoizer(final Object object, final int size, final long expireMillis) {
		this.object = object;
		this.expireMillis = expireMillis;
		this.cache = allocCache(size);
	}

	private static final Map<CacheKey, CacheValue> allocCache(final int maxSize) {
		return Collections.synchronizedMap(new LinkedHashMap<CacheKey, CacheValue>() {
			private static final long serialVersionUID = 42L;

			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<CacheKey, CacheValue> eldest) {
				return size() > maxSize;
			}
		});
	}

	/**
	 * Internal method
	 */
	@Override
	public Object invoke(final Object proxy, //
			final Method method, //
			final Object[] args) throws Throwable {
		if (method.getReturnType().equals(Void.TYPE)) {
			// Don't cache void methods
			return invoke(method, args);
		} else {
			final CacheKey key = new CacheKey(method, Arrays.asList(args));
			CacheValue cacheValue = cache.get(key);
			if (cacheValue != null) {
				final Object ret = cacheValue.getValueIfNotExpired();
				if (ret != CacheValue.EXPIRED) {
					return ret;
				}
			}
			final Object ret = invoke(method, args);
			cache.put(key, new CacheValue(ret, expireMillis));
			return ret;
		}
	}

	private Object invoke(final Method method, final Object[] args) //
			throws Throwable {
		try {
			return method.invoke(object, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private static final class CacheKey {
		private final Method method;
		private final List<Object> params;

		public CacheKey(final Method method, final List<Object> params) {
			this.method = method;
			this.params = params;
		}

		@Override
		public int hashCode() {
			return (method.hashCode() ^ params.hashCode());
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof CacheKey) {
				final CacheKey o = (CacheKey) obj;
				return o.method.equals(this.method) && //
						o.params.equals(this.params);
			}
			return false;
		}
	}

	private static final class CacheValue {
		private static final Object EXPIRED = new Object();
		private final Object value;
		private final long expire;

		public CacheValue(final Object value, final long expire) {
			this.value = value;
			this.expire = System.currentTimeMillis() + expire;
		}

		public Object getValueIfNotExpired() {
			if (expire > System.currentTimeMillis()) {
				return value;
			}
			return EXPIRED;
		}
	}
}
