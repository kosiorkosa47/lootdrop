<script lang="ts">
	import '../app.css';
	import type { Snippet } from 'svelte';
	import { onMount } from 'svelte';
	import SplashScreen from '$lib/components/SplashScreen.svelte';
	import BottomNav from '$lib/components/BottomNav.svelte';
	import { isSplashVisible } from '$lib/stores/app.svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();

	const showApp = $derived(!isSplashVisible());

	onMount(async () => {
		// Register service worker
		if ('serviceWorker' in navigator) {
			navigator.serviceWorker.register('/sw.js').catch((err) => {
				if (import.meta.env.DEV) {
					console.warn('SW registration failed:', err);
				}
			});
		}

		// Web Vitals — log to console in dev, could send to analytics in prod
		const { onCLS, onFID, onLCP, onFCP, onTTFB } = await import('web-vitals');
		const logVital = (metric: { name: string; value: number; rating: string }) => {
			if (import.meta.env.DEV) {
				console.log(`[Web Vital] ${metric.name}: ${metric.value.toFixed(1)} (${metric.rating})`);
			}
		};
		onCLS(logVital);
		onFID(logVital);
		onLCP(logVital);
		onFCP(logVital);
		onTTFB(logVital);
	});
</script>

<SplashScreen />

{#if showApp}
	<main
		class="min-h-screen"
		style="
			padding-top: var(--sat);
			padding-bottom: calc(var(--nav-bar-height) + var(--sab));
			animation: fade-in 0.3s cubic-bezier(0.2, 0, 0, 1);
		"
	>
		{@render children()}
	</main>

	<BottomNav />
{/if}
