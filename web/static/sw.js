/// @ts-nocheck
const CACHE_NAME = 'lootdrop-v2';
const STATIC_CACHE = 'lootdrop-static-v2';
const DYNAMIC_CACHE = 'lootdrop-dynamic-v2';

/** Assets to pre-cache on install */
const PRECACHE_URLS = [
	'/',
	'/manifest.json',
	'/icons/icon-192.png',
	'/icons/icon-512.png',
	'https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap'
];

/** Install: pre-cache shell assets */
self.addEventListener('install', (event) => {
	event.waitUntil(
		caches
			.open(STATIC_CACHE)
			.then((cache) => cache.addAll(PRECACHE_URLS))
			.then(() => self.skipWaiting())
	);
});

/** Activate: clean old caches */
self.addEventListener('activate', (event) => {
	event.waitUntil(
		caches.keys().then((keys) => {
			return Promise.all(
				keys
					.filter((key) => key !== STATIC_CACHE && key !== DYNAMIC_CACHE)
					.map((key) => caches.delete(key))
			);
		}).then(() => self.clients.claim())
	);
});

/**
 * Fetch strategy:
 * - Static assets (js, css, images, fonts): cache-first
 * - API calls and navigation: network-first with cache fallback
 */
self.addEventListener('fetch', (event) => {
	const { request } = event;
	const url = new URL(request.url);

	// Skip non-GET requests
	if (request.method !== 'GET') return;

	// Skip chrome-extension and other non-http schemes
	if (!url.protocol.startsWith('http')) return;

	// API requests: network-first
	if (url.pathname.startsWith('/api/')) {
		event.respondWith(networkFirst(request, DYNAMIC_CACHE));
		return;
	}

	// Static assets: cache-first
	if (isStaticAsset(url.pathname)) {
		event.respondWith(cacheFirst(request, STATIC_CACHE));
		return;
	}

	// Navigation and everything else: network-first
	event.respondWith(networkFirst(request, DYNAMIC_CACHE));
});

/** Cache-first: return cached version, fall back to network */
async function cacheFirst(request, cacheName) {
	const cached = await caches.match(request);
	if (cached) return cached;

	try {
		const response = await fetch(request);
		if (response.ok) {
			const cache = await caches.open(cacheName);
			cache.put(request, response.clone());
		}
		return response;
	} catch {
		return new Response('Offline', { status: 503, statusText: 'Service Unavailable' });
	}
}

/** Network-first: try network, fall back to cache */
async function networkFirst(request, cacheName) {
	try {
		const response = await fetch(request);
		if (response.ok) {
			const cache = await caches.open(cacheName);
			cache.put(request, response.clone());
		}
		return response;
	} catch {
		const cached = await caches.match(request);
		if (cached) return cached;

		// For navigation requests, return the cached index page (SPA fallback)
		if (request.mode === 'navigate') {
			const fallback = await caches.match('/');
			if (fallback) return fallback;
		}

		return new Response('Offline', { status: 503, statusText: 'Service Unavailable' });
	}
}

/** Check if a path is a static asset */
function isStaticAsset(pathname) {
	return /\.(js|css|svg|png|jpg|jpeg|webp|woff2?|ttf|ico)$/i.test(pathname);
}
